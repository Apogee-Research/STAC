#!/usr/bin/env python

"""Analyze a series of responses in a PCAP file."""

import os
from collections import namedtuple
from itertools import izip_longest
import json
import socket
import struct

import pcap
import dpkt

from util import getLogger, Results

_logger = getLogger(__name__)


# Number of PSH packets before and after edge events
NUM_LEADING_PKTS = 6
NUM_TRAILING_PKTS = 2


class Event(namedtuple('Event', ('dt', 'tcp_len'))):

    def json_encode(self):
        return [self.dt, self.tcp_len]

    @classmethod
    def json_decode(cls, lst):
        assert isinstance(lst, list)
        return cls(*lst)


class Response(list):
    """Wrapper for a list of packets making up a single reponse."""

    def edge_events(self):
        def generate():
            for (seq, event) in enumerate(self):
                if seq >= NUM_LEADING_PKTS:
                    if event.tcp_len < 300:
                        yield event
                    else:
                        break
        return list(generate())

    def edge_timings(self):
        return [event.dt for event in self.edge_events()]

    def json_encode(self):
        return self

    @classmethod
    def json_decode(cls, lst):
        assert isinstance(lst, list)
        return Response(Event.json_decode(o) for o in lst)


class Responses(list):

    @classmethod
    def parse(cls, pcap_file_or_queries_dir):
        """Parse responses from a .pcap file."""
        if os.path.isdir(pcap_file_or_queries_dir):
            pcap_file = os.path.join(pcap_file_or_queries_dir, 'responses.pcap')
        else:
            pcap_file = pcap_file_or_queries_dir

        pc = pcap.pcap(pcap_file)

        # Figure out how to extract IP packets based on datalink type
        datalink = pc.datalink()
        if datalink == pcap.DLT_NULL:   # BSD/Mac OS X loopback
            def parse_ip(buf):
                af = struct.unpack_from('<H', buf)[0]   # assume little-endian
                if af == 2:   # AF_INET
                    return dpkt.ip.IP(buf[4:])
        elif datalink == pcap.DLT_EN10MB:
            def parse_ip(buf):
                eth = dpkt.ethernet.Ethernet(buf)
                if eth.type == dpkt.ethernet.ETH_TYPE_IP:
                    return eth.data
        elif datalink == pcap.DLT_RAW:
            def parse_ip(buf):
                return dpkt.ip.IP(buf)
        else:
            raise RuntimeError, "unknown datalink type %d" % datalink

        events = None
        ts_prev = 0

        for (ts, buf) in pc:
            ip = parse_ip(buf)
            if not ip:
                continue

            if ip.p == dpkt.ip.IP_PROTO_TCP:
                tcp = ip.data
            else:
                continue

            # Include only packets *from* a GraphHopper server (port 8989)
            src = socket.inet_ntoa(ip.src)
            if tcp.sport != 8989:
                continue

            # On SYN, emit current response and start a new one
            if (tcp.flags & dpkt.tcp.TH_SYN):
                if events:
                    yield Response(events)
                events = []
                continue

            # From this point on consider only TCP PUSH packets
            if not (tcp.flags & dpkt.tcp.TH_PUSH):
                continue

            dt = 0 if ts_prev == 0 else ts - ts_prev
            ts_prev = ts

            tcp_len = len(tcp.data)
            events.append(Event(dt, tcp_len))

        if events:
            yield Response(events)   # yield final response

    @classmethod
    def convert(cls, data_dir):
        pcap_file = os.path.join(data_dir, 'responses.pcap')
        _logger.info("parsing responses from %s", pcap_file)
        output_file = os.path.join(data_dir, 'responses.jsonl')
        _logger.info("writing parsed responses to %s", output_file)
        with open(output_file, 'w') as fp:
            for response in cls.parse(pcap_file):
                print >> fp, json.dumps(response.json_encode())

    @classmethod
    def lazy_load(cls, data_dir):
        input_file = os.path.join(data_dir, 'responses.jsonl')
        _logger.info("loading responses from %s", input_file)
        with open(input_file) as fp:
            for line in fp:
                yield Response.json_decode(json.loads(line))

    @classmethod
    def load(cls, data_dir):
        return cls(cls.lazy_load(data_dir))

    def zip_with_queries(self, queries, discard=True):
        num_responses = 0
        num_bad = 0
        for (i, (response, query)) in enumerate(izip_longest(self, queries)):
            if response is None or query is None:
                # probably means we loaded a non-corresponding pair of
                # response and query files
                raise RuntimeError("mismatched number of responses vs. queries")
            num_responses += 1
            if len(response) != (NUM_LEADING_PKTS + len(query) - 1 + NUM_TRAILING_PKTS):
                _logger.warn("response %d length does not match query; discarding", i)
                num_bad += 1
                if discard:
                    continue
                else:
                    response = None
            yield (response, query)
        _logger.warn("%d/%d bad responses (%.3g%%)",
                     num_bad, num_responses,
                     float(num_bad)/float(num_responses) * 100.0)

    def bad_coeff(self, queries):
        import numpy as np
        from matrix import Matrix
        assert len(self) == len(queries)
        matrix = Matrix.load()
        data = np.zeros((2, len(self)))
        for (i, (response, query)) in enumerate(self.zip_with_queries(queries, discard=False)):
            mst_edges = matrix.mst_edges(query)
            data[0, i] = sum(index for (index, _edge) in mst_edges)
            data[1, i] = 1.0 if response is None else 0.0
        return np.corrcoef(data)


if __name__ == '__main__':
    import argh

    def convert(data_dir):
        Responses.convert(data_dir)

    def packets(responses):
        from pprint import pprint
        pprint(Responses.load(responses))

    def edge_events(responses):
        for (i, response) in enumerate(Responses.load(responses)):
            print "response %d" % i
            for event in response.edge_events():
                print "  %s" % (event,)

    def bad_coeff(data_dir):
        from queries import Queries
        responses = Responses.load(data_dir)
        queries = Queries.load(data_dir)
        return responses.bad_coeff(queries)

    import argh
    argh.dispatch_commands([convert, packets, edge_events, bad_coeff])
