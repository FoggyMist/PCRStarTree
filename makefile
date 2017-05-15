CLASS_PATH = -cp bin/.
SOURCE_PATH = -sourcepath src/.

LAUNCH_FLAGS = -cp bin/.
COMPILE_FLAGS = $(CLASS_PATH) $(SOURCE_PATH) -d bin/

COMPILER = javac
LAUNCHER = java

MAIN_NAME = Main
MAIN_SOURCE = ./src/$(MAIN_NAME).java
MAIN_OBJECT = ./bin/$(MAIN_NAME).class

SOURCES = $(filter-out $(MAIN_SOURCE), $(shell find -name *.java))
OBJECTS = $(patsubst ./src%,./bin%, $(patsubst %.java,%.class, $(SOURCES)));
#$(subst engine/,engine/bin/,
# SRC_FILES := $(filter-out src/bar.cpp, $(SRC_FILES))

all: main


main: $(MAIN_OBJECT)
	@echo Launching: $^
	@$(LAUNCHER) $(LAUNCH_FLAGS) $(MAIN_NAME)

$(MAIN_OBJECT): $(MAIN_SOURCE)
	@echo Recompiling: $^
	@$(COMPILER) $(COMPILE_FLAGS) $^

$(MAIN_SOURCE): $(OBJECTS)

bin/trees/%.class : src/trees/%.java
	@echo Compiling: $^
	@$(COMPILER) $(COMPILE_FLAGS) $^


clear:
	@rm -f -r -d bin/*
