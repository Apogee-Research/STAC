import argparse
import interact_gab
import random
import string

if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("hostname", help="The hostname")
    parser.add_argument("port", type=int, help="The port used by GabFeed")
    parser.add_argument("username", help="The user's username")
    parser.add_argument("password", help="The user's password")
    parser.add_argument("numOfMessages", type=int, help="The number of messages the user will post")
    args = parser.parse_args()

    session = interact_gab.get_session()
    gab_caller = interact_gab.GabCaller(args.hostname, args.port, args.username, args.password)

    # authenticate and log in
    auth_resp = interact_gab.GabCaller.do_authenticate(gab_caller, session)
    login_resp = interact_gab.GabCaller.do_login(gab_caller, session)

    # navigate to the correct room
    response = interact_gab.GabCaller.go_to_room(gab_caller, session, login_resp.url, "1")
    response = interact_gab.GabCaller.go_to_thread(gab_caller, session, response.url, "1")

    # add the desired number of messages
    for num in range(args.numOfMessages):
        interact_gab.GabCaller.post_message(gab_caller, session, response.url, "1", "1", str(num), False)