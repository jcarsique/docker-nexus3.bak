
include ../make.d/skaffold.mk

.PHONY: all build base

all: skaffold@up build skaffold@down

build: base

base: skaffold.yaml~gen
	$(SKAFFOLD) build -f skaffold.yaml~gen

