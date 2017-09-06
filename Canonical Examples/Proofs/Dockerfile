FROM localhost:5000/stac_base:v6
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
ADD ["Category 1", "/home/Category 1"]
ADD ["Category 2", "/home/Category 2"]
ADD ["Category 3", "/home/Category 3"]
ADD ["Category 4", "/home/Category 4"]
ADD ["Category 5", "/home/Category 5"]
ADD ["Category 6", "/home/Category 6"]
ADD ["Category 7", "/home/Category 7"]
ADD ["Category 8", "/home/Category 8"]
ADD ["Category 9", "/home/Category 9"]
ADD ["Category 10", "/home/Category 10"]
ADD ["Category 11", "/home/Category 11"]
ADD ["Category 12", "/home/Category 12"]
ADD ["Category 13", "/home/Category 13"]
ADD ["Category 14", "/home/Category 14"]
ADD ["Category 15", "/home/Category 15"]
ADD ["Category 16", "/home/Category 16"]
WORKDIR /home/
