import re
import sys
sys.path.append('../benign')

import interact

CITY_AND_ACCESS_POINT_PATTERN = re.compile('"([^"]+)","([^"]+)"')
CITY_RESPONSE_PATTERN = re.compile('.*Your current location [^"]+?"([^"]*)".*', re.MULTILINE | re.DOTALL)


def all_cities_from_file(city_file):
    with open(city_file) as file:
        for line in file:
            match = CITY_AND_ACCESS_POINT_PATTERN.match(line)
            if match:
                # yield (city, access_points)
                yield match.groups()

def all_cities_from_server(hostname, port):
    url = "https://{}:{}/cities".format(hostname, port)
    with interact.get_session() as session:
        response = session.get(url)
        for line in response.text.splitlines():
            match = CITY_AND_ACCESS_POINT_PATTERN.match(line)
            if match:
                yield match.groups()

class FindCities(object):
    '''
    The cities_file should be a CSV file with two columns.
    The first column is the city name and the second column
    is a comma-separated list of access points for that city.
    '''

    def __init__(self, hostname, port, username, password):
        '''
        Creates an instance.

        :param hostname: name of the server
        :param port: int port number of the server
        :param username: name of client user
        :param password: password of client user
        '''
        self.hostname = hostname
        self.port = port
        ignore_image_loading = True
        self.snap_caller = interact.SnapCaller(hostname, port, username, password, ignore_image_loading)

    def find_all_cities(self):
        '''
        Sets the location using all the cities in the file
        '''
        for city, access_points in all_cities_from_server(self.hostname, self.port):
            foundCity = self.find_one_city(access_points)
            assert (foundCity == city)
            print "City: {}, AP length: {}".format(foundCity, len(access_points))

    def find_one_city(self, access_points):
        '''
        Connects and interacts with the SnapBuddy client.
        The sequence of calls is as follows:
        1. Call "/" and get redirected to the /authenticate page
        2. POST to /authenticate and get redirected to the /login page
        3. POST to /login and get redirected to the /location/set page
        4. POST to /location/set and get redirected to the /location/confirm page
        5. End of session
        If the location confirmation page is posted, then the number of
        times the user changes their location will be incremented and
        that is constrained.  By stopping before confirming, this
        limitation can be bypassed.

        Assertions are included for each call to verify that the
        expected flow is followed.


        Returns the city as specified by the server
        '''
        with interact.get_session() as session:
            response = self.snap_caller.do_authenticate(session)
            response = self.snap_caller.do_login(session, response.url)
            response = self.snap_caller.do_set_location(session, access_points, response.url)

            return CITY_RESPONSE_PATTERN.match(response.text).group(1)

if __name__ == "__main__":
    if len(sys.argv) < 5:
        print "findcities.py <hostname> <port> <username> <password>"
        sys.exit(-1)
    caller = FindCities(sys.argv[1], int(sys.argv[2]), sys.argv[3], sys.argv[4])
    caller.find_all_cities()
