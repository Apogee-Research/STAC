FROM localhost:5000/stac_base:v5
RUN sed -i -e "s/\(^hosts:.*\)/#\1\nhosts: files/" /etc/nsswitch.conf
RUN adduser stac
RUN mkdir /home/stac/challenge_program
ADD APIsUsed.txt /home/stac
ADD challenge_program/ipchallenge-0.1.jar /home/stac/challenge_program
ADD challenge_program/images /var/lib/trainer/images/
ADD description.txt /home/stac
ADD examples /home/stac/examples
ADD questions /home/stac/questions
RUN chown -R stac:stac /home/stac
USER stac
WORKDIR /home/stac
