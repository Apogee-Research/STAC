Since the side channel relies on observing the size of the Subspace
server's database file, the exploit script must be run on the same
host (i.e., within the same Docker container) as the server.

This directory contains a Dockerfile that builds an image layered on
top of the challenge image, with the addition of an exploit user "mal"
and the exploit scripts from this directory. Unlike the blue team user
"stac", the exploit user does not have read permission for the
server's database file.

In the container, you must first run `register-exploit-user.sh`, then
`exploit.py`. If you get a 400 Bad Request HTTP error when running the
latter, you probably forgot to run the former.

Details of the vulnerability and exploit script are provided in
`EL/fulldescription.txt` and `EL/budgets/budgets.txt`.
