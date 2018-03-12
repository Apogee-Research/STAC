FROM localhost:5000/stac_base:v5
RUN adduser stac
ADD challenge_program.tar /home/stac
RUN chown -R stac:stac /home/stac
USER stac
WORKDIR /home/stac
