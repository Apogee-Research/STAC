import argparse
import sys

sys.path.append("../../../examples")
import interact_gab



if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("hostname", help="The hostname")
    parser.add_argument("port", type=int, help="The port used by GabFeed")
    parser.add_argument("username", help="The user's username")
    parser.add_argument("password", help="The user's password")
    args = parser.parse_args()

    session = interact_gab.get_session()
    gab_caller = interact_gab.GabCaller(args.hostname, args.port, args.username, args.password)

    # authenticate and log in
    auth_resp = interact_gab.GabCaller.do_authenticate(gab_caller, session)
    login_resp = interact_gab.GabCaller.do_login(gab_caller, session)

    # add the desired number of messages
    for num in range(9 * 10):
        interact_gab.GabCaller.post_message(gab_caller, session, login_resp.url, "1", "1", "abcd", False)
