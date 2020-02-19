
  Pod::Spec.new do |s|
    s.name = 'FujitsuThermalprinterPlugin'
    s.version = '0.0.1'
    s.summary = 'Custom Capacitor Plugin for Fujitsu Thermal Printer'
    s.license = 'MIT'
    s.homepage = 'https://github.com/robyrenata/fujitsu-thermalprinter-plugin.git'
    s.author = 'robyrenata'
    s.source = { :git => 'https://github.com/robyrenata/fujitsu-thermalprinter-plugin.git', :tag => s.version.to_s }
    s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}'
    s.ios.deployment_target  = '11.0'
    s.dependency 'Capacitor'
  end