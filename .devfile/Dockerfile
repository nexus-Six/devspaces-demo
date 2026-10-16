# Tools image for Dev Spaces: OpenJDK 21 + git (UBI openjdk image has no git).
FROM registry.access.redhat.com/ubi9/openjdk-21:latest

USER root
RUN microdnf install -y git \
    && microdnf clean all \
    && git --version

USER 185
