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

DIRS = %w{gaeshi gaeshi-dev lein-gaeshi}

DIRS.each do |dir|

  namespace dir do
    desc "full #{dir} build"
    task :build do
      in_dir dir do
        run_command "lein deps, javac"
        run_command "lein spec"
      end
    end

    desc "push to clojars"
    task :push do
      in_dir dir do
        run_command "lein push"
      end
    end

    desc "install locally"
    task :install do
      in_dir dir do
        run_command "lein install"
      end
    end
  end

end


namespace "lein-gaeshi" do

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

  task :build => %w{init}
end

desc "build all projects"
task :build => DIRS.map {|dir| "#{dir}:build"}

desc "push all projects"
task :push => DIRS.map {|dir| "#{dir}:push"}

desc "install all projects"
task :install => DIRS.map {|dir| "#{dir}:install"}

task :default => :build