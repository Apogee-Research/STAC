FROM localhost:5000/stac_base:v5
RUN yum install elinks -y
RUN sed -i -e "s/\(^hosts:.*\)/#\1\nhosts: files/" /etc/nsswitch.conf
ADD APIsUsed.txt /home
ADD challenge_program /home/challenge_program
ADD description.txt /home
ADD examples /home/examples
ADD questions /home/questions
WORKDIR /home
