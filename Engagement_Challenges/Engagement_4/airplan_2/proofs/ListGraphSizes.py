#!/usr/bin/env python

from lxml import html
import requests
from requests.packages.urllib3.exceptions import InsecureRequestWarning

#requests.packages.urllib3.disable_warnings(InsecureRequestWarning)

def generateGraph(size):
	l = ['<vertex name="{}"/>'.format(i) for i in range(1,size+1)]
	return '<graph>' + ''.join(l) + '</graph>'

def listAllGraphURLs(s, maxSize):
	page = s.get('https://127.0.0.1:8443', verify=False)
	tree = html.fromstring(page.content)
	return [tree.xpath("/html/body/main/section/div[3]/ul[{}]/li/a/@href".format(x))[0] for x in range(1,maxSize+1)]

def listAllGraphMatrixURLs(s, graphURLs):
	return ['/flight_matrix/' + url[7:] for url in graphURLs]

def printAllPacketSizes(s, urlList):
	counter = 1
	for url in urlList:
		r = s.get('https://127.0.0.1:8443' + url, verify=False)
		print("{} vertices: ".format(counter) + r.headers.get('content-length'))
		counter += 1

def login(s):
	s.post('https://127.0.0.1:8443/login', files={"username":"southeast", "password":"df89gy9Qw"}, verify=False)

def uploadGraphs(s, maxSize):
	for size in range(1,maxSize+1):
		graph = generateGraph(size)
		route_map_name = '{}'.format('a'*size)
		s.post('https://127.0.0.1:8443/add_route_map', files={'file' : ('graph.xml',graph,'text/xml'), 'route_map_name':route_map_name}, verify=False)

def main():
	maxSize = int(input("Please enter the size of the biggest graph you want to test: "))
	s = requests.session()
	login(s)
	uploadGraphs(s, maxSize)
	graphURLs = listAllGraphURLs(s, maxSize)
	graphMatrixURLs = listAllGraphMatrixURLs(s, graphURLs)
	printAllPacketSizes(s, graphMatrixURLs)

if __name__ == "__main__":
	main()