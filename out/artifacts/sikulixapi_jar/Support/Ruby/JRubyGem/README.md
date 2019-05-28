Sikuli JRubyGem 2019 (version 1.1.4+)
===

Implements the Ruby gem for using the SikuliX Java API in JRuby scripts.

This is only needed if you want to use your own JRuby installation to run scripts or use other Ruby IDE`s.

In the SikuliX IDE you can create, edit and run Ruby scripts as well and also run from command line.

## Where to get

Install it using 

```
jgem install sikulix
```
This should install the latest gem version (1.1.4.0 as of April 2019)
 
### Prerequisits
* Download the SikuliX IDE and SikuliX API [jars from here](https://raiman.github.io/SikuliX1/downloads.html) 

### Running scripts using JRuby

* Set SIKULIXAPI_JAR environment variable to sikulixapi.jar with full path<br />examples:

  ```
  Windows: set SIKULIXAPI_JAR=c:\...\...\..\sikulixapi.jar
  Mac/Linux: export SIKULIXAPI_JAR=/.../.../../sikulixapi.jar
  ```
* Create ruby scripts with the following lines at the beginning

    ```ruby
    require 'sikulix'
    include Sikulix

    # place your code here

    ```
* Run it with JRuby

## Special for Ruby scripting

* After 'include Sikulix' it is possible to use “undotted” methods. E.g. click(), exists(), etc in global context.
* Registration of hot-keys:

    ```ruby
    addHotkey( Key::F1, KeyModifier::ALT + KeyModifier::CTRL) do
      popup 'hallo', 'Title'
    end
    ```
* Registration of events:

    ```ruby
    onAppear("123.png") { |e| popup 'hi', 'title' }
    # ...
    observe 10
    ```
* Alternative events registration:

    ```ruby
    # event with lambda with a parameter
    hnd = ->(e) {popup(e.inspect, 'hi!')}
    onAppear "123.png", &hnd
    # ...
    observe
    ```
* Creating objects of Sikuli classes without explicit constructor

    ```ruby
    ptn = Pattern("123.png").similar(0.67)
    ```

* Possibility to enumerate array

    ```ruby
    findAll("123.png").each do |obj|
      puts obj.getTarget.toString()
    end
    ```

## Other docs

**Usage docs now on [ReadTheDocs](http://sikulix-2014.readthedocs.org/en/latest/#)**
