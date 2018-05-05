# Block editor

School project to IJA class (The Java Programming Language). Application is mathematical scheme editor.
Every scheme is set  of graphical blocks connected by links.There is specified few block types,
every type hold different mathematical operation (+, - * etc.).Block contains input and output ports.
Block holds numeral value and distributes it through output port to other block.
That is basic idea how this application works.

## Authors

* **Tomáš Kukaň** - *xkukan00*
* **Petr Knetl** - *xknetl00*

## Building the application

For compile of application run...

```
user@machine:/path/to/project$
```

For generating of Java doc documentation run...
```
user@machine:/path/to/project$
```

For creating zip archive of project run...
```
user@machine:/path/to/project$
```
For remove of all built files run...
```
user@machine:/path/to/project$
```

## Running the application
...

```
user@machine:/path/to/project$ 
```

## Controls of GUI application

### Menubar
Top bar menu is composed by 'File...' dropdown menu and two buttons ('Run' and 'Debug').

* *File...* dropdown: 
  * Load - load existing scheme from file
  * Save - save current scheme into file

* *Run* button - Compute whole scheme if possible.
* *Debug* button - Compute one block of scheme and stops.

### Mouse Control
Block can be added to scheme by dragging icon from left panel to right scene.
Moving with blocks is possible as Drag-and-drop action. New link is created
after Drag-and-drop from right half of output block into input port of another block.
Block can be deleted by clicking on cross in his top-right corner.



