FROM localhost:5000/stac_base:v5
RUN yum makecache fast
RUN yum install -y wget
RUN yum install -y vim-enhanced.x86_64
RUN yum install -y tar
RUN yum install -y bzip2
RUN yum install tcpdump -y
WORKDIR /tmp
RUN wget https://repo.continuum.io/archive/Anaconda3-4.2.0-Linux-x86_64.sh
RUN chmod 755 Anaconda3-4.2.0-Linux-x86_64.sh
RUN HOME=/home && ./Anaconda3-4.2.0-Linux-x86_64.sh -b
WORKDIR /usr/local/bin/
RUN ln -s /home/anaconda3/bin/python3.5 ./python3.5
RUN /home/anaconda3/bin/pip install scapy-python3
ADD ["Analysis and Proofs", "/home/Analysis and Proofs"]
WORKDIR /home/

