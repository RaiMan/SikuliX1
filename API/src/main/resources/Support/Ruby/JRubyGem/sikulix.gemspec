Gem::Specification.new do |s|
  s.name        = 'sikulix'
  s.version     = '1.1.0.3'
  s.date        = '2014-09-10'
  s.summary     = 'Sikulix gem'
  s.description = 'A wrapper over SikuliX java lib (sikulixapi.jar)'
  s.authors     = ['Roman S Samarev', 'Raimund Hocke']
  s.email       = 'rssdev10@gmail.com'
  s.files       = [
                   'sikulix.rb',
                   'sikulix/platform.rb',
                   'sikulix/sikulix.rb'
                  ].map {|f| 'lib/' + f}

  s.homepage    = 'http://sikulix.com'
  s.license     = 'MIT'
end
