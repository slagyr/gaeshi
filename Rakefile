def run_command(command)
  system command
  exit_code = $?.exitstatus
  if exit_code != 0
    raise "Command failed with code #{exit_code}: #{command}"
  else
    puts "Command executed successfully: #{command}"
  end
end

def in_dir(path)
  pwd = Dir.getwd
  Dir.chdir path
  yield
ensure
  Dir.chdir pwd
end

namespace :gaeshi do
  desc "full gaeshi build"
  task :build do
    in_dir "gaeshi" do
      run_command "lein deps, javac"
      run_command "lein spec"
    end
  end

  desc "push to clojars"
  task :push do
    in_dir "gaeshi" do
      run_command "lein push"
    end
  end
end

namespace :gaeshi_dev do
  desc "full gaeshi-dev build"
  task :build do
    in_dir "gaeshi-dev" do
      run_command "lein deps, javac"
      run_command "lein spec"
    end
  end

  desc "push to clojars"
  task :push do
    in_dir "gaeshi-dev" do
      run_command "lein push"
    end
  end
end

namespace :lein_gaeshi do

  desc "init lein-gaeshi"
  task :init do
    in_dir "lein-gaeshi" do
      if !File.exists?("leiningen-1.7.0-standalone.jar")
        puts "downloading Leiningen"
        run_command "wget https://github.com/downloads/technomancy/leiningen/leiningen-1.7.0-standalone.jar"
      else
        puts "Leiningen already downloaded"
      end
    end
  end

  desc "full lein-gaeshi build"
  task :build => %w{init} do
    in_dir "lein-gaeshi" do
      run_command "lein deps, javac"
      run_command "lein spec"
    end
  end

  desc "push to clojars"
  task :push do
    in_dir "lein-gaeshi" do
      run_command "lein push"
    end
  end
end

desc "full build"
task :build => %w{gaeshi:build gaeshi_dev:build lein_gaeshi:build}
task :push => %w{gaeshi:push gaeshi_dev:push lein_gaeshi:push}

task :default => :build