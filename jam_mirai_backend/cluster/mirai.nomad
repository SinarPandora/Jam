job "mirai-backends" {
  datacenters = ["dc1"]

  group "mirai" {
    count = 1
    network {
      port "cq-http" {
        static = 5700
      }
    }

    task "client" {
      driver = "raw_exec"

      config {
        command = "<your path>\\go-cqhttp_windows_amd64.exe"
      }
    }
  }
}