
include skaffold.mk

.PHONY: all build base skaffold.yaml~gen

all: skaffold@up build skaffold@down

build: base

base: skaffold.yaml~gen
	$(call SKAFFOLD, build -f skaffold.yaml~gen)

