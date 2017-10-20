import argparse
import re
import requests
import sys



DEFAULT_USER_PUBLIC_KEY = "12345"

def get_session():
    session = requests.Session()
    session.verify = False  # Don't verify the server's SSL cert
    # Turn off logging due to not verifying SSL cert; not generally a good idea but okay for this app
    requests.packages.urllib3.disable_warnings(requests.packages.urllib3.exceptions.InsecureRequestWarning)
    #requests.packages.urllib3.disable_warnings(requests.packages.urllib3.exceptions.InsecurePlatformWarning)
    return session

class GabCaller(object):
    '''
    The configuration and calling of the SnapBuddy client.
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
        self.username = username
        self.password = password
        

    def get_root_url(self):
        # Set the initial root page URL (https://hostname:port/)
        return "https://{}:{}/".format(self.hostname, self.port)

    def do_authenticate(self, session, user_public_key=DEFAULT_USER_PUBLIC_KEY, url=None):
        if not url:
            url = self.get_root_url()

        response = session.get(url)

        # Response should be a redirect to the authentication page
        assert len(response.history) == 1 and response.history[0].is_redirect
        assert re.search("authenticate", response.url)

        # The response URL points to the authenticate page
        # Use the URL associated with that response to POST the
        # public key value to the authenticate page.
        response = session.post(response.url, files={"A": user_public_key})

        # Response should be a redirect to (and GET of) the login page
        assert len(response.history) == 1 and response.history[0].is_redirect
        assert re.search("login", response.url)
        
        # Return the URL from the redirect (/login)
        return response

    def do_login(self, session, url=None):
        if not url:
            url = self.get_root_url() + 'login'

        response = session.post(url, files={"username": self.username, "password": self.password})

        # Response should be a redirect to "/" then to the rooms page
        assert re.search("rooms", response.url)
        
        # Return the URL from the redirect (/rooms)
        return response

    def do_search(self, session, search_term):
        url = self.get_root_url() + "search"
        response = session.post(url, files={"search": search_term, "submit": "search"})
 
        assert len(response.history) == 0

        assert re.search("search", response.url)

        # Return the URL (/search)
        return response

    def create_thread(self, session, room, thread_name, msg):
        url = self.get_root_url() + "newthread/" + room      
        response = session.post(url, files = {"messageContents": msg, "threadName": thread_name, "submit": "Post"})
        assert len(response.history) == 1
        assert response.history[0].is_redirect
        return response

    def go_to_room(self, session, url, room_num):
        response = session.get(url + "/" + room_num)
        assert re.search("room", response.url)

        # Return the URL from the redirect (/search/resultss)
        return response

    def go_to_thread(self, session, url, thread_num):
        response = session.get(url + "_" + thread_num)
        return response
        
    def post_message(self, session, url, room, thread, msg, allowRedirects=True):
        url = self.get_root_url() + "newmessage/" + room + "_" + thread
        response = session.post(url, files = {"messageContents": msg, "submit": "Post"}, allow_redirects=allowRedirects)
        return response

# user logs into GabFeed and does a variety of actions, including several term of the day searches
if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("hostname", help="The hostname")
    parser.add_argument("port", type=int, help="The port used by GabFeed")
    parser.add_argument("username", help="The user's username")
    parser.add_argument("password", help="The user's password")
    parser.add_argument("room1", help="The first room to access the message thread")
    parser.add_argument("thread", help="The thread within the first room to post messages")
    parser.add_argument("msg1", help="The message to post in the first room's thread")
    parser.add_argument("room2", help="The second room to create a new message thread")
    parser.add_argument("thread_name", help="The name of the thread within the second room to create")
    parser.add_argument("msg2", help="The message to post in the second room's thread")
    parser.add_argument("terms", nargs='*', help="Search terms")
    parser.add_argument("-k", "--key", help="The user's public key", default=DEFAULT_USER_PUBLIC_KEY)
    args = parser.parse_args()

    caller = GabCaller(args.hostname, args.port, args.username, args.password)
    with get_session() as session:
        auth_resp = caller.do_authenticate(session, args.key)
        login_resp = caller.do_login(session, auth_resp.url)

        # go to specified thread of specified room and post message msg
        response = caller.go_to_room(session, login_resp.url, args.room1)
        response = caller.go_to_thread(session, response.url, args.thread)
        response = caller.post_message(session, response.url, args.room1, args.thread, args.msg1)

        # create a new thread
        response = caller.create_thread(session, args.room2, args.thread_name, args.msg2)

        # do searches
        searches = sys.argv[11:]
        for search in searches:
            response = caller.do_search(session, search)
