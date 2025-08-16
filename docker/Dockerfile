FROM ubuntu:latest

WORKDIR /app

RUN apt-get update && \
    apt-get install -y openjdk-17-jre nginx supervisor cron locales wget && \
    rm -rf /var/lib/apt/lists/*

RUN mkdir -p /app/input /app/output /var/log/supervisor

COPY run.sh .

COPY nginx.conf /etc/nginx/sites-available/default
RUN rm /etc/nginx/sites-enabled/default
RUN ln -s /etc/nginx/sites-available/default /etc/nginx/sites-enabled/default

COPY supervisord.conf /etc/supervisor/conf.d/supervisord.conf

COPY crontab /etc/cron.d/my-cronjob

RUN chmod +x run.sh && \
    chmod 0644 /etc/cron.d/my-cronjob && \
    crontab /etc/cron.d/my-cronjob

EXPOSE 80

CMD ["/usr/bin/supervisord", "-n", "-c", "/etc/supervisor/supervisord.conf"]
