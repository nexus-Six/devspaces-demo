# Tools image for Dev Spaces: OpenJDK 17 + git (UBI openjdk image has no git).
# v1 maintenance line — intentionally different from main (JDK 21).
FROM registry.access.redhat.com/ubi9/openjdk-17:latest

USER root
RUN microdnf install -y git \
    && microdnf clean all \
    && git --version

USER 185
