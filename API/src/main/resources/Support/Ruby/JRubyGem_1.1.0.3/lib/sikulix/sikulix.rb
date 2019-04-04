# coding: utf-8
# SikuliX for Ruby

require 'java'

# Classes and methods for using SikuliX
module Sikulix
  private
  # 'private' for avoiding of unexpected effects when
  #   'include Sikulix' is used.
  java_import java.net.Socket
  java_import org.sikuli.script.Sikulix
  java_import org.sikuli.script.Screen
  java_import org.sikuli.script.Region
  java_import org.sikuli.script.ScreenUnion

  java_import org.sikuli.script.Observing
  java_import org.sikuli.script.ObserverCallBack

  java_import org.sikuli.script.Constants
  java_import org.sikuli.script.Finder
  java_import org.sikuli.script.ImageFinder
  java_import org.sikuli.script.ImageFind

  java_import org.sikuli.script.Button
  java_import org.sikuli.basics.OS

  java_import org.sikuli.script.Match
  java_import org.sikuli.script.Pattern
  java_import org.sikuli.script.Location

  java_import org.sikuli.script.ImagePath
  java_import org.sikuli.script.Image
  java_import org.sikuli.script.ImageGroup

  java_import org.sikuli.script.App
  java_import org.sikuli.script.Key
  java_import org.sikuli.script.KeyModifier
  java_import org.sikuli.script.Mouse

  java_import org.sikuli.basics.Settings
  java_import org.sikuli.basics.ExtensionManager

  java_import org.sikuli.script.compare.DistanceComparator
  java_import org.sikuli.script.compare.VerticalComparator
  java_import org.sikuli.script.compare.HorizontalComparator

  java_import org.sikuli.basics.Debug

	$SCRIPT_SUPPORT = true
  begin
    java_import org.sikuli.scriptrunner.ScriptRunner
  rescue
		$SCRIPT_SUPPORT = false
  end
  java_import org.sikuli.script.Runner
  java_import org.sikuli.script.RunTime
	$RUNTIME = RunTime.get()

  #
  # This method generates a wrapper for Java Native exception processing
  # in native java methods. It allows to detect a line number in script
  # that opened in IDE where the exception was appearing.
  #
  # obj - class for the wrapping
  # methods_array - array of method names as Symbols
  def self.native_exception_protect(obj, methods_array)
    methods_array.each do |name|
      m = obj.instance_method name
      obj.class_exec do
        alias_method(('java_' + name.to_s).to_sym, name)
        define_method(name) do |*args|
          begin
            # java specific call for unbound methods
            m.bind(self).call(*args)
          rescue NativeException => e
            raise StandardError, e.message
          end
        end
      end
    end
  end

  # Dynamic method definition for several cases of invocation
  def self.dynamic_def(name, &block)
    # using as:
    #   include Sikulix
    #   some_method(...)
    define_method(name, &block)
    # using as:
    #   Sikulix::some_method(...)
    define_singleton_method(name, &block)

    # private method to avoid of attachment to all subclasses
    private name
  end

  # Wrap following java-methods by an exception processor
  native_exception_protect(
    Region,
    [:find, :findAll, :wait, :waitVanish, :exists,
     :click, :doubleClick, :rightClick, :hover, :dragDrop,
     :type, :paste, :observe]
   )

  # Default screen of host machine.
  $SIKULI_SCREEN = Screen.new
  # Screen used for undotted methods.
  $DEFAULT_SCREEN = $SIKULI_SCREEN

  # Replaces default screen for which all undotted methods are
  # called with another screen
  # example:
  # setDefaultScreen($SIKULI_SCREEN)
  # click(Location(10,10)) <- click will be performed on local screen
  def setDefaultScreen(screen)
    if screen.respond_to?(:click)
      $DEFAULT_SCREEN = screen
      Debug.log("Screen switched")
    end
  end

# This is an alternative for method generation using define_method
#  # Generate hash of ('method name'=>method)
#  # for all possible "undotted" methods.
#  UNDOTTED_METHODS =
#    [$SIKULI_SCREEN, Sikulix].reduce({}) do |h, obj|
#      h.merge!(
#        obj.methods.reduce({}) do |h2, name|
#          h2.merge!(name => obj.method(name))
#        end
#      )
#    end

  # It makes possible to use java-constants as a methods
  # Example: Key.CTRL instead of Key::CTRL
  [Key, KeyModifier].each do |obj|
    obj.class_exec do
      def self.method_missing(name)
        if (val = const_get(name))
          return val
        end
        fails "method missing #{name}"
      end
    end
  end

  # Generate static methods in Sikulix context
  # for possible "undotted" methods.
  Sikulix.java_class.java_class_methods.map(&:name).uniq.each do |name|
    obj_method = Sikulix.method(name)
    dynamic_def(name) { |*args, &block| obj_method.call(*args, &block) }
  end

  $SIKULI_SCREEN.java_class.java_instance_methods.map(&:name).uniq.each do |name|
    dynamic_def(name) { |*args, &block| $DEFAULT_SCREEN.method(name).call(*args, &block) }
  end

  # TODO: check it after Env Java-class refactoring
  java_import org.sikuli.script.Env
  java_import org.sikuli.basics.HotkeyListener

  class Env  # :nodoc: all
    class RHotkeyListener < HotkeyListener
      def initialize(block)
        super()
        @block = block
      end

      def hotkeyPressed(event)
        @block.call event
      end
    end
  end

  ##
  # Register hotkeys
  #
  # Example:
  #    addHotkey( Key::F1, KeyModifier::ALT + KeyModifier::CTRL ) do
  #      popup 'hallo', 'Title'
  #    end
  #
  def addHotkey(key, modifiers, &block)
    Env.addHotkey key, modifiers, Env::RHotkeyListener.new(block)
  end

  ##
  # Unregister hotkeys
  #
  # Example:
  #    removeHotkey( Key::F1, KeyModifier::ALT + KeyModifier::CTRL )
  def removeHotkey(key, modifiers)
    Env.removeHotkey key, modifiers
  end

  # Generate methods like constructors.
  # Example: Pattern("123.png").similar(0.5)
  [Pattern, Region, Screen, App].each do |cl|
    name = cl.java_class.simple_name
    dynamic_def(name) { |*args| cl.new(*args) }
  end
  dynamic_def("Location") { |*args| Location.new(*args).setOtherScreen($DEFAULT_SCREEN) }
end

# This is an alternative for method generation using define_method
## This method allow to call "undotted" methods that belong to
## Region/Screen or SikuliX classes.
# def self.method_missing(name, *args, &block)
#
#  if (method = Sikulix::UNDOTTED_METHODS[name])
#    begin
#      ret = method.call(*args, &block)
#      # Dynamic methods that throw a native Java-exception,
#      # hide a line number in the scriptfile!
#      # Object.send(:define_method, name){ |*args| method.call(*args) }
#      return ret
#    rescue NativeException => e
#      raise StandardError, "Sikulix: Problem (#{e})\n" \
#        "with undotted method: #{name} (#{args})"
#    end
#  else
#    fail "undotted method '#{name}' missing"
#  end
# end
