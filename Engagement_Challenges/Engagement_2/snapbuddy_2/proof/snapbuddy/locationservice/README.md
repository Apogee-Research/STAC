processpackets.py is a tool that can be used to get size and timing
side channel information from a captured SSL session.

It can be used at the commandline or the get_transactions() method can be
used programatically for further analysis.

Usage:

```
python processpackets.py <pcap filename> <server ip address> <server port>
```

Example

```
python processpackets.py test3.pcap 127.0.0.1 8080
127.0.0.1:37501 -> 127.0.0.1:8080
	Req: 480 Resp: 5920 Duration: 0.00853991508484
127.0.0.1:37501 -> 127.0.0.1:8080
	Req: 512 Resp: 91600 Duration: 1.16804409027
127.0.0.1:37502 -> 127.0.0.1:8080
	Req: 512 Resp: 50736 Duration: 1.17614293098
127.0.0.1:37503 -> 127.0.0.1:8080
	Req: 512 Resp: 69584 Duration: 0.0756719112396
127.0.0.1:37504 -> 127.0.0.1:8080
	Req: 512 Resp: 70752 Duration: 3.57399296761
127.0.0.1:37505 -> 127.0.0.1:8080
	Req: 512 Resp: 53328 Duration: 6.24951195717
```

Packets can be captured like so:

```
sudo tcpdump -i lo -w test.pcap
```

Location attack:

Start the SnapBuddy web client and then capture packets:

```
sudo tcpdump -i lo -w <pcap filename> port <server port>
```

Then, run the following two scripts (terminating the tcpdump inbetween).

```
python findcities.py <server ip address> <server port> <username> <pw> <csv filename> 
python location.py <pcap filename> <server ip address> <server port>
```

Note: For the loopback address, it may be useful to specify it as
127.0.0.1 instead of localhost.

The first one parses the city CSV file and calls /location/set
for each set of access points.  The second script takes the
captured pcap file and writes out the request and response sizes
for each /location/set posting.

The output of the first script may be pasted to the output from the
second script to match the city with the corresponding request size.

