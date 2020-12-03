from bs4 import BeautifulSoup
import requests
import re
import os
import shutil

def get_id(name, year):
	raw_response = requests.get(f'https://www.imdb.com/find?q={name}&ref_=nv_sr_sm').content
	regex = re.search(f'<a href=\"/title/(tt\d+)/\" >[^<]+</a> \({year}\)', raw_response.decode())
	if regex:
		return regex.group(1)
	raise Exception(f"Could not find {name}")

def get_slate_wrapper(id):
	raw_response = requests.get(f'https://www.imdb.com/title/{id}/?ref_=fn_al_tt_1').content
	soup = BeautifulSoup(raw_response.decode(), "html.parser")
	sw = soup.find("div", class_="slate_wrapper")
	if sw:
		return sw
	sw = soup.find("div", class_="posterWithPlotSummary")
	if sw:
		return sw
	raise Exception(f"Could not find slate for {id}")

def get_poster(slate_wrapper):
	poster_dir = slate_wrapper.find("div", class_="poster").a['href']
	raw_response = requests.get(f'https://www.imdb.com{poster_dir}').content
	soup = BeautifulSoup(raw_response.decode(), "html.parser")
	img_url = soup.find("img", attrs={"data-image-id": f"{poster_dir.split('/')[-1]}-curr"})
	if img_url and img_url['src']:
		return img_url['src']
	raise Exception(f"Could not img url for {id}")

def save_image(url, path):
	r = requests.get(url, stream=True)
	if r.status_code == 200:
		with open(path, 'wb') as f:
			r.raw.decode_content = True
			shutil.copyfileobj(r.raw, f)

def get_trailer(slate_wrapper):
	video_url = slate_wrapper.find("div", class_="slate").a['href']
	if video_url:
		return video_url
	raise Exception(f"Could not video url for {id}")

if __name__ == "__main__":
	movie_list = open("movies.txt").read().split("\n")
	names_years = [(name.split('; ')[0],name.split('; ')[1].strip(),name.split(';')[-2].strip()) for name in movie_list]

	for movie in names_years:
		### FETCH IDS ###
		imdb_id = get_id(movie[0],movie[1])
		print(imdb_id)
		
		### FETCH SLATE ###
		slate = get_slate_wrapper(imdb_id)

		## FETCH POSTER ###
		img_url = get_poster(slate)
		print(img_url)
		save_image(img_url, f"posters/{imdb_id}.jpg")

		### FETCH TRAILER ###
		video_url = get_trailer(slate)
		print(video_url)
		