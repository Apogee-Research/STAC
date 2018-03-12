import argparse
import airplan_client

if __name__=="__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument("serverip", help="The server's ip address")
    parser.add_argument("serverport", help="The server's port")
    parser.add_argument("username", help="Airport's username")
    parser.add_argument("password", help="Airport's password")
    parser.add_argument("route_map_file", help="File containing route map to upload")
    parser.add_argument("route_map_name", help="Name of route map once it is uploaded")
    parser.add_argument("origin", help="Origin airport for shortest path calculation")
    parser.add_argument("destination", help="Destination airport for shortest path calculation")
    parser.add_argument("weightType", help="Weight type for shortest path calculation")

    args = parser.parse_args()

    airplan = airplan_client.AirPlanClient(args.serverip, args.serverport, args.username, args.password)
    with airplan_client.get_session() as session:
        airplan.login(session)
        airplan.uploadRouteMap(session, args.route_map_file, args.route_map_name)
        response = airplan.shortestPath(session, args.origin, args.destination, args.weightType)
        print response.text

