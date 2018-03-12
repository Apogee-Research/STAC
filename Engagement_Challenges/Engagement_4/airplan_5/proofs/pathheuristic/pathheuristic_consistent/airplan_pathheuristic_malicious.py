#!/usr/bin/env python
import argparse
import airplan_client

def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("serverip", help="The server's ip address")
    parser.add_argument("serverport", help="The server's port")
    parser.add_argument("username", help="The airport's username")
    parser.add_argument("password", help="The airport's password")
    parser.add_argument("graph_file", help="File containing the graph to upload")
    parser.add_argument("graph_name", help="Name of graph once it is uploaded")
    parser.add_argument("source", help="Source vertex's id")
    parser.add_argument("destination", help="Destination vertex's id")
    parser.add_argument("weight_type", help="The type of weight to use in the path heuristic")
    args = parser.parse_args()

    airplan = airplan_client.AirPlanClient(args.serverip, args.serverport, args.username, args.password)

    with airplan_client.get_session() as session:
        airplan.login(session)
        airplan.uploadRouteMap(session, args.graph_file, args.graph_name)
        response = airplan.shortestPath(session, args.source, args.destination, args.weight_type)
        print response.text


if __name__ == "__main__":
    main()
