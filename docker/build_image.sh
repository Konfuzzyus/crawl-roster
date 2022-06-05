#!/usr/bin/env bash

cp ../build/distributions/*.tar .
BUILDKIT=1 docker build . -t crawl-roster:local
