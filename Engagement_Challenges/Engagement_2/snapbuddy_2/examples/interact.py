import argparse
import re
import requests
import sys

DEFAULT_USER_PUBLIC_KEY = "12345"

IMAGE_PATTERN = re.compile('<img src="([^"]+?)"', (re.MULTILINE | re.DOTALL))


def get_session():
    session = requests.Session()
    session.verify = False  # Don't verify the server's SSL cert

    # Turn off logging due to not verifying SSL cert; not generally a good idea but okay for this app
    requests.packages.urllib3.disable_warnings(requests.packages.urllib3.exceptions.InsecureRequestWarning)
    #requests.packages.urllib3.disable_warnings(requests.packages.urllib3.exceptions.InsecurePlatformWarning)

    return session


class SnapCaller(object):
    '''
    The configuration and calling of the SnapBuddy client.
    This is meant to represent the interation of a typical user
    with the system - as if they are accessing these pages
    from a browser.
    '''

    def __init__(self, hostname, port, username, password, ignore_images=False):
        '''
        Creates an instance.

        :param hostname: name of the server
        :param port: int port number of the server
        :param username: name of client user
        :param password: password of client user
        :param ignore_images: true if image loading should be ignored
        '''
        self.hostname = hostname
        self.port = port
        self.username = username
        self.password = password
        self.ignore_images = ignore_images

    def get_root_url(self):
        '''
        :return: root page URL (https://hostname:port/)
        '''
        return "https://{}:{}/".format(self.hostname, self.port)

    def process(self, access_points="", user_public_key=DEFAULT_USER_PUBLIC_KEY):
        '''
        Connects and interacts with the SnapBuddy client.
        This ensures a session is created and then the user goes
        through the entire login process.
        :param access_points: comma separated list of location access points
        :param user_public_key: key used to authenticate
        :return: response from call to do_friends
        '''
        with get_session() as session:
            return self.process_login(session, access_points, user_public_key)

    def process_login(self, session, access_points="", user_public_key=DEFAULT_USER_PUBLIC_KEY):
        '''
        Using the specified session and access points,
        access the web client and walk through the login process.
        :param session: used to maintain user state
        :param access_points: comma separated list of location access points
        :param user_public_key: key used to authenticate
        :return: response from call to GET /friends
        '''
        response = self.do_authenticate(session, user_public_key)
        response = self.do_login(session, response.url)
        response = self.do_set_location(session, access_points, response.url)
        response = self.do_confirm_location(session, response.url)

        # Response holds the response from GET /friends
        return response

    def do_authenticate(self, session, user_public_key=DEFAULT_USER_PUBLIC_KEY, url=None):
        if not url:
            url = self.get_root_url()

        response = session.get(url)

        # Response should be a redirect to the authentication page
        assert len(response.history) == 1 and response.history[0].is_redirect
        assert re.search("authenticate", response.url)

        self.load_images(session, response)

        # The response url points to the authentication page.
        # Use the URL associated with that response to POST the
        # public key value to the authenticate page.
        response = session.post(response.url, files={"A": user_public_key})

        # Response should be a redirect to (and GET of) the login page
        assert len(response.history) == 1 and response.history[0].is_redirect
        assert re.search("login", response.url)

        self.load_images(session, response)

        # Return the response from the redirect (/login)
        return response

    def do_login(self, session, url=None):
        if not url:
            url = self.get_root_url() + 'login'

        response = session.post(url, files={"username": self.username, "password": self.password})

        # Response should be a redirect to (and GET of) the set initial location page
        assert len(response.history) == 1 and response.history[0].is_redirect
        assert re.search("location/set", response.url)

        self.load_images(session, response)

        # Return the response from the redirect (/location/set)
        return response

    def do_set_location(self, session, access_points="", url=None):
        if not url:
            url = self.get_root_url() + 'location/set'

        response = session.post(url, files={"bssids": access_points})

        # Response should be a redirect to (and GET of) the confirm location page
        # (along with the requested location id)
        assert len(response.history) == 1 and response.history[0].is_redirect
        assert re.search("location/confirm", response.url)

        self.load_images(session, response)

        # Return the response from the redirect (/location/confirm)
        return response

    def do_confirm_location(self, session, url=None):
        if not url:
            url = self.get_root_url() + 'location/confirm'

        identity = ""
        match = re.search(r"lid=(\w+)", url)
        if match:
            identity = match.group(1)

        response = session.post(url, files={"identity": identity})

        # Response should be a redirect to (and GET of) the default (/friends) page
        assert len(response.history) >= 1
        for item in response.history:
            assert item.is_redirect
        assert re.search("/friends", response.url)

        self.load_images(session, response)

        # Return the response from the redirect (/friends)
        return response

    def do_friends(self, session, url=None):
        if not url:
            url = self.get_root_url() + 'friends'

        response = session.get(url)

        self.load_images(session, response)

        return response

    def do_invite(self, session, url=None):
        if not url:
            url = self.get_root_url() + 'invite'

        response = session.get(url)

        self.load_images(session, response)

        return response

    def do_filter(self, session, image_url, filter_ids):
        '''
        :param session: the session to use
        :param image_url: the relative path of the image, usually <user id>/<image name>.jpg
        :param filter_ids: the filter ids to apply
        :return:
        '''
        url = self.get_root_url() + "filter/" + image_url
        payload = [("filter list", filter_id) for filter_id in filter_ids]
        print payload
        return session.post(url, files=payload)

    def do_image(self, session, image_url):
        url = self.get_root_url() + image_url.lstrip('/')  # Remove any leading slash
        return session.get(url)

    def load_images(self, session, response):
        if not self.ignore_images:
            for image_url in IMAGE_PATTERN.findall(response.text):
                self.do_image(session, image_url)

    def __repr__(self):
        return "server: {}:{} user: {} pw: {}".format(self.hostname, self.port,
                                                      self.username, self.password)


if __name__ == "__main__":
    parser = argparse.ArgumentParser(description="Simulates user interaction with SnapBuddy")
    parser.add_argument("hostname", help="Hostname or ip address where SnapBuddy is running")
    parser.add_argument("port", type=int, help="Port value used by SnapBuddy")
    parser.add_argument("username", help="The user's username")
    parser.add_argument("password", help="The user's password")
    parser.add_argument("-i", "--ignore", action="store_true", help="Do not load <img> requests within responses; default is %(default)s: load all images")
    parser.add_argument("-p", "--accessPoints", help="Comma separated list of Location access points", default="")
    parser.add_argument("-k", "--key", help="The user's public key", default=DEFAULT_USER_PUBLIC_KEY)
    args = parser.parse_args()

    caller = SnapCaller(args.hostname, args.port, args.username, args.password, args.ignore)
    response = caller.process(args.accessPoints, args.key)

    print response.url
