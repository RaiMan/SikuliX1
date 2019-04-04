require 'java'

module Sikulix
  class Platform
    def self.check_and_require
      begin
        # check running from IDE
        java_import org.sikuli.basics.Sikulix
      rescue
        # check external jar
        sikulix_path = (ENV['SIKULIXAPI_JAR'] || ENV['SIKULIX_HOME'] || "").chomp(' ')
        unless File.exist? sikulix_path
          raise LoadError, "Failed to load #{sikulix_path}\nMake sure SIKULIXAPI_JAR is set!"
        end
        require sikulix_path
      end
      return true
    end
  end
end
