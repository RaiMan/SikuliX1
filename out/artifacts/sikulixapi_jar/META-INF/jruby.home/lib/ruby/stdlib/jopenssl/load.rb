warn 'Loading jruby-openssl gem in a non-JRuby interpreter' unless defined? JRUBY_VERSION

require 'java'
require 'jopenssl/version'

warn "JRuby #{JRUBY_VERSION} is not supported by jruby-openssl #{Jopenssl::VERSION}" if JRUBY_VERSION < '1.7.20'

# NOTE: assuming user does pull in BC .jars from somewhere else on the CP
unless ENV_JAVA['jruby.openssl.load.jars'].eql?('false')
  version = Jopenssl::BOUNCY_CASTLE_VERSION
  bc_jars = nil
  begin
    require 'jar-dependencies'
    # if we have jar-dependencies we let it track the jars
    require_jar( 'org.bouncycastle', 'bcprov-jdk15on', version )
    require_jar( 'org.bouncycastle', 'bcpkix-jdk15on', version )
    require_jar( 'org.bouncycastle', 'bctls-jdk15on',  version )
    bc_jars = true
  rescue LoadError
    bc_jars = false
  end
  unless bc_jars
    load "org/bouncycastle/bcprov-jdk15on/#{version}/bcprov-jdk15on-#{version}.jar"
    load "org/bouncycastle/bcpkix-jdk15on/#{version}/bcpkix-jdk15on-#{version}.jar"
    load "org/bouncycastle/bctls-jdk15on/#{version}/bctls-jdk15on-#{version}.jar"
  end
end

require 'jruby'
require 'jopenssl.jar'
org.jruby.ext.openssl.OpenSSL.load(JRuby.runtime)

if RUBY_VERSION > '2.3'
  load 'jopenssl23/openssl.rb'
  load 'jopenssl24.rb' if RUBY_VERSION >= '2.4'
elsif RUBY_VERSION > '2.2'
  load 'jopenssl22/openssl.rb'
elsif RUBY_VERSION > '2.1'
  load 'jopenssl21/openssl.rb'
else
  load 'jopenssl19/openssl.rb'
end

require 'openssl/pkcs12'
