#!/bin/sh
set -e
cp /etc/prometheus/prometheus-template.yml /tmp/prometheus.yml
sed -i "s|\${ACTUATOR_USER}|${ACTUATOR_USER}|g" /tmp/prometheus.yml
sed -i "s|\${ACTUATOR_PASSWORD}|${ACTUATOR_PASSWORD}|g" /tmp/prometheus.yml
exec /bin/prometheus --config.file=/tmp/prometheus.yml --storage.tsdb.path=/prometheus "$@"
