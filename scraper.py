#################################################
#  IMDB WebScraper V2.1 By Alexander Jacobsen   #
#################################################

from bs4 import BeautifulSoup
from multiprocessing import Pool
import requests
import re
import os
import json
import shutil
import traceback

class IMDBscraper:
	def __init__(self, imdb_id=None, name=None, year=None, search_type='tv'):
		self._name = name
		self._year = year
		self._id = imdb_id
		self.search_type = search_type
		self._page = None
		self._slate = None
		self._poster_url = None
		self._summary = None
		self._cast_info = None
		self._run_time = None
		self._length = None
		self._num_seasons = None
		self._seasons = None

	@property
	def id(self):
		if not self._id:
			self._id = self._get_id()
		return self._id

	@property
	def name(self):
		if not self._name:
			try:
				self._name = self._get_title()
			except Exception as e:
				print(e)
				return "NULL"
		return self._name

	@property
	def year(self):
		if not self._year:
			try:
				self._year = self._get_run_time().split("-")[0].strip()
			except Exception as e:
				print(e)
				return "NULL"
		return self._year

	@property
	def page(self):
		if not self._page:
			self._page = self._get_page()
		return self._page

	@property
	def slate(self):
		if not self._slate:
			try:
				self._slate = self._get_slate_wrapper()
			except Exception as e:
				print(e)
				return BeautifulSoup("<html></html>", "html.parser")
		return self._slate

	@property
	def poster_url(self):
		if not self._poster_url:
			try:
				self._poster_url = self._get_poster_url()
			except Exception as e:
				print(e)
				return None
		return self._poster_url

	@property
	def summary(self):
		if not self._summary:
			try:
				self._summary = self._get_summary()
			except Exception as e:
				print(e)
				return "NULL"
		return self._summary

	@property
	def cast_info(self):
		if not self._cast_info:
			try:
				self._cast_info = self._get_cast_info()
			except Exception as e:
				print(e)
				return {}
		return self._cast_info

	@property
	def length(self):
		if not self._length:
			try:
				self._length = self._get_length()
			except Exception as e:
				print(e)
				return "NULL"
		return self._length

	@property
	def run_time(self):
		if not self._run_time:
			try:
				self._run_time = self._get_run_time()
			except Exception as e:
				print(e)
				return "NULL"
		return self._run_time

	@property
	def num_seasons(self):
		if not self._num_seasons:
			try:
				self._num_seasons = self._get_num_seasons()
			except Exception as e:
				print(e)
				return 0
		return self._num_seasons

	@property
	def seasons(self):
		if not self._seasons:
			try:
				self._seasons = self._get_seasons()
			except Exception as e:
				print(e)
				print(traceback.format_exc())
				return {}
		return self._seasons


	def _get_id(self):
		raw_response = requests.get(f'https://www.imdb.com/find?q={self.name} {self.year}&s=tt&ttype={self.search_type}').content
		regex = re.search(f'<a href=\"/title/(tt\d+)/\" >[^<]+</a> \({self.year}\)', raw_response.decode())
		if regex:
			return regex.group(1)
		raise Exception(f"Could not find {self.name}")

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
		if self.poster_url:
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
			summary_text = ''.join(tag.find_all(text=True, recursive=True)).strip()
			if "See full summary" in summary_text:
				raw_response = requests.get(f'https://www.imdb.com/title/{self.id}/plotsummary').content
				soup = BeautifulSoup(raw_response.decode(), "html.parser")
				full_summary = soup.find("ul", id="plot-summaries-content")
				if full_summary and full_summary.li.p:
					return ''.join(full_summary.li.p.find_all(text=True, recursive=True)).strip()
			else:
				return summary_text
				
		raise Exception(f"Could not find summary for {self.id}")

	def _get_cast_info(self):
		ps = self._get_plot_summary()
		info_list = ps.find_all("div", class_="credit_summary_item")
		if info_list:
			raw_list_info = {info.h4.string[:-1]:list(info.find_all("a")) for info in info_list}
			return {str(key):[str(tag.string) for tag in value if tag.string != "See full cast & crew" and "more credit" not in tag.string] for key,value in raw_list_info.items()}
			
		raise Exception(f"Could not find cast info for {self.id}")

	def _get_title_wrapper(self):
		ps = self.page.find("div", class_="title_wrapper")
		if ps:
			return ps
		raise Exception(f"Could not find title_wrapper for {self.id}")

	def _get_title(self):
		ps = self._get_title_wrapper()
		return ps.h1.string.strip()

	def _get_length(self):
		ps = self._get_title_wrapper()
		tag = ps.find("div", class_="subtext")
		if tag:
			return ''.join(tag.find("time")).strip()
		raise Exception(f"Could not find length for {self.id}")
		
	def _get_run_time(self):
		ps = self._get_title_wrapper()
		tag = ps.find("div", class_="subtext")
		if tag:
			return tag.find("a", title="See more release dates").string.replace("TV Series", "").strip()
		raise Exception(f"Could not find run time for {self.id}")

	def _get_num_seasons(self):
		raw_response = requests.get(f"https://www.imdb.com/title/{self.id}/episodes?season=1")
		if raw_response.status_code == 200:
			soup = BeautifulSoup(raw_response.content.decode(), "html.parser")
			dropdown = soup.find("select", id="bySeason", class_="current", tconst=self.id)
			if dropdown:
				return len(dropdown.find_all("option"))
		raise Exception(f"Could not find page for {self.id}")

	def _get_episodes(self, season):
		raw_response = requests.get(f"https://www.imdb.com/title/{self.id}/episodes?season={season}")
		if raw_response.status_code == 200:
			soup = BeautifulSoup(raw_response.content.decode(), "html.parser")
			eps = []
			for ep_num, ep_element in enumerate(soup.find("div", class_="list detail eplist").find_all("div", class_="list_item")):
				#print(ep_num+1, re.findall(r"Ep\d+", ep_element.div.a.div.div.string))
				eps.append(re.findall(r"tt\d+", ep_element.find("a", itemprop="name")['href'])[0])
			return eps
		raise Exception(f"Could not find season {season} for {self.id}")

	def _get_seasons(self):
		seasons = {}
		for sn in range(self.num_seasons):
			seasons[sn+1] = self._get_episodes(sn+1)
		return seasons


def fetch_movie(movie):
	imdb = IMDBscraper(name=movie[0], year=movie[1], search_type='ft')
	tmp_info = {}
	tmp_info["MovieID"] = imdb.id
	tmp_info["Title"] = movie[0]
	tmp_info["People"] = imdb.cast_info
	tmp_info["Summary"] = imdb.summary
	tmp_info["Length"] = imdb.length
	tmp_info["Year"] = movie[1]
	tmp_info["Genre"] = movie[2]
	tmp_info["Score"] = movie[3]
	tmp_info["Trailer"] = movie[5] 
	#if f"{imdb.id}.jpg" not in os.listdir("posters"):
	#	img_url = imdb.poster_url
	#	imdb.save_poster(f"posters/{imdb.id}.jpg")
	return tmp_info

def fetch_show(show):
	imdb = IMDBscraper(name=show[0], year=show[1].split("-")[0], search_type='tv')
	tmp_info = {}
	tmp_info["ShowID"] = imdb.id
	tmp_info["Title"] = imdb._get_title()
	tmp_info["Year"] = imdb.run_time
	tmp_info["Summary"] = imdb.summary
	tmp_info["Score"] = show[3]
	tmp_info["Length"] = imdb.length
	tmp_info["Genre"] = show[2]
	tmp_info["People"] = imdb.cast_info
	tmp_info["Seasons"] = imdb.seasons
	if f"{imdb.id}.jpg" not in os.listdir("posters"):
		img_url = imdb.poster_url
		imdb.save_poster(f"posters/{imdb.id}.jpg")
	return tmp_info

def fetch_episode(inf):
	show_info = inf[0]
	season_num = inf[1]
	episode_num = inf[2]
	episode_id = inf[3]
	ep_scraper = IMDBscraper(imdb_id=episode_id)
	tmp_info = {}
	tmp_info["EpisodeID"] = episode_id
	tmp_info["EpisodeNumber"] = episode_num
	tmp_info["Season"] = season_num
	tmp_info["ShowName"] = show_info["Title"]
	tmp_info["Title"] = ep_scraper.name
	tmp_info["Summary"] = ep_scraper.summary
	tmp_info["Length"] = ep_scraper.length
	tmp_info["People"] = ep_scraper.cast_info
	tmp_info["ShowID"] = show_info["ShowID"]
	if f"{episode_id}.jpg" not in os.listdir("thumbnails"):
		ep_img_url = ep_scraper.poster_url
		ep_scraper.save_poster(f"thumbnails/{ep_scraper.id}.jpg")
	return tmp_info


if __name__ == "__main__":
	txtfile = open("movies.txt", "r").read().split("\n")
	movie_list = [[item for item in line.split("; ")] for line in txtfile]

	txtfile = open("shows.txt", "r").read().split("\n")
	show_list = [[item for item in line.split("; ")] for line in txtfile]

	m_info = dict()
	s_info = dict()
	e_info = dict()

	with Pool(processes=16) as pool:
		for movie_dict in pool.imap_unordered(fetch_movie, movie_list):
			m_info[movie_dict["MovieID"]] = movie_dict
			print("Movie:", movie_dict["Title"], movie_dict["MovieID"])

	open("movies_test.json", "w", encoding="utf-8").write(json.dumps(m_info))

	#with Pool(processes=16) as pool:
	#	for show_dict in pool.imap_unordered(fetch_show, show_list):
	#		s_info[show_dict["ShowID"]] = show_dict
	#		print("Show:", show_dict["Title"], show_dict["ShowID"])
	#		for episode_dict in pool.imap_unordered(fetch_episode, [(show_dict, season_num, ep_num+1, episode) for season_num, episodes in show_dict["Seasons"].items() for ep_num, episode in enumerate(episodes)]):
	#			e_info[episode_dict["EpisodeID"]] = episode_dict
	#			print("Episode:", episode_dict["EpisodeNumber"], "Season:", episode_dict["Season"], episode_dict["EpisodeID"])

	#open("shows.json", "w", encoding="utf-8").write(json.dumps(s_info))
	#open("episodes.json", "w", encoding="utf-8").write(json.dumps(e_info))

