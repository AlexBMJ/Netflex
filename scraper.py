##########################
#   Written By AlexBMJ   #
##########################

from bs4 import BeautifulSoup
import requests
import re
import os
import json
import shutil

class IMDBscraper:
	def __init__(self, name, year):
		self.name = name
		self.year = year
		self._id = None
		self._page = None
		self._slate = None
		self._poster_url = None
		self._summary = None
		self._cast_info = None

	@property
	def id(self):
		if not self._id:
			self._id = self._get_id()
		return self._id

	@property
	def page(self):
		if not self._page:
			self._page = self._get_page()
		return self._page

	@property
	def slate(self):
		if not self._slate:
			self._slate = self._get_slate_wrapper()
		return self._slate

	@property
	def poster_url(self):
		if not self._poster_url:
			self._poster_url = self._get_poster_url()
		return self._poster_url

	@property
	def summary(self):
		if not self._summary:
			self._summary = self._get_summary()
		return self._summary

	@property
	def cast_info(self):
		if not self._cast_info:
			self._cast_info = self._get_cast_info()
		return self._cast_info

	def _get_id(self):
		raw_response = requests.get(f'https://www.imdb.com/find?q={self.name}&ref_=nv_sr_sm').content
		regex = re.search(f'<a href=\"/title/(tt\d+)/\" >[^<]+</a> \({self.year}\)', raw_response.decode())
		if regex:
			return regex.group(1)
		raise Exception(f"Could not find {name}")

	def _get_page(self):
		raw_response = requests.get(f'https://www.imdb.com/title/{self.id}')
		if raw_response.status_code == 200:
			return BeautifulSoup(raw_response.content.decode(), "html.parser")
		raise Exception(f"Could not find page for {self.id}")

	def _get_slate_wrapper(self):
		sw = self.page.find("div", class_="slate_wrapper")
		if sw:
			return sw
		sw = self.page.find("div", class_="posterWithPlotSummary")
		if sw:
			return sw
		raise Exception(f"Could not find slate for {self.id}")

	def _get_poster_url(self):
		poster_dir = self.slate.find("div", class_="poster").a['href']
		raw_response = requests.get(f'https://www.imdb.com{poster_dir}').content
		soup = BeautifulSoup(raw_response.decode(), "html.parser")
		img_url = soup.find("img", attrs={"data-image-id": f"{poster_dir.split('/')[-1]}-curr"})
		if img_url and img_url['src']:
			return img_url['src']
		raise Exception(f"Could not img url for {self.id}")

	def save_poster(self, path):
		r = requests.get(self.poster_url, stream=True)
		if r.status_code == 200:
			with open(path, 'wb') as f:
				r.raw.decode_content = True
				shutil.copyfileobj(r.raw, f)

	def _get_trailer(self, name, year):
		headers = {'User-Agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:83.0) Gecko/20100101 Firefox/83.0', 'Accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8', 'Accept-Language': 'en-US', 'Cache-Control': 'no-cache'}
		raw_response = requests.get("https://html.duckduckgo.com/html/", params=[("q", f"{name} {year} Trailer site:youtube.com")], headers=headers)
		if raw_response.status_code != 200:
			raise Exception(f"Throttled...")
		soup = BeautifulSoup(raw_response.content.decode(), "html.parser")
		video_url = soup.find("a", class_="result__a")
		if video_url and video_url['href']:
			return video_url['href']
		raise Exception(f"Could not video url for {self.id}")

	def _get_plot_summary(self):
		ps = self.page.find("div", class_="plot_summary")
		if ps:
			return ps
		raise Exception(f"Could not find summary info for {self.id}")

	def _get_summary(self):
		ps = self._get_plot_summary()
		tag = ps.find("div", class_="summary_text")
		if tag:
			return ''.join(tag.find_all(text=True, recursive=True)).strip()
		raise Exception(f"Could not find summary for {self.id}")

	def _get_cast_info(self):
		ps = self._get_plot_summary()
		info_list = ps.find_all("div", class_="credit_summary_item")
		return {info.h4.string[:-1]:[tag.string for tag in info.find_all("a")] for info in info_list}
	

if __name__ == "__main__":
	txtfile = open("movies.txt").read().split("\n")
	movie_list = [[item for item in line.split("; ")] for line in txtfile]

	info = dict()

	for movie in movie_list:
		imdb = IMDBscraper(movie[0], movie[1])
		imdb._id = movie[4]
		### FETCH IDS ###
#		print(imdb.id)

		## FETCH POSTER ###
#		img_url = imdb.poster_url
#		print(img_url)
#		imdb.save_poster(f"posters/{imdb.id}.jpg")

		### FETCH TRAILER ###
#		video_url = get_trailer(movie[0], movie[1])
#		print(movie[0], video_url)

		### FETCH SUMMARY ###
		info[imdb.id] = imdb.cast_info
		info[imdb.id]["Summary"] = imdb.summary
		info[imdb.id]["Title"] = movie[0]
		info[imdb.id]["Year"] = movie[1]
		info[imdb.id]["Genre"] = movie[2]
		info[imdb.id]["Score"] = movie[3]
		info[imdb.id]["Trailer"] = movie[5]
		print(info[imdb.id])

open("movies.json", "w", encoding="utf-8").write(json.dumps(info))