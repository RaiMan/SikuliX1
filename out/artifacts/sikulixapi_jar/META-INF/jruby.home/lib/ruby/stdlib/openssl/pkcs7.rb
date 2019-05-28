if RUBY_VERSION > '1.9'
  raise LoadError, "no such library in #{RUBY_VERSION}: openssl/pkcs7"
else
  load "jopenssl18/openssl/pkcs7.rb"
end