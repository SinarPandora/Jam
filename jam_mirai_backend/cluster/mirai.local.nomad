job "mirai-backends" {
  datacenters = ["dc1"]

  # ------------------------------------- JAM ----------------------------------------------
  group "mirai-jam" {
    count = 1
    network {
      port "cq-http" {
        static = 5900
      }
    }

    task "client" {
      driver = "raw_exec"
      resources {
        cpu    = 1000
        memory = 300
      }

      config {
        command = "bash.exe"
        args    = [
          "-c",
          "cd H:/jam/jam/go_backend && ./go-cqhttp_1.0.0_windows_amd64.exe"
        ]
      }
    }
  }
  # ----------------------------------- ARFIES --------------------------------------------
  group "mirai-arfies" {
    count = 1
    network {
      port "cq-http" {
        static = 5701
      }
    }

    task "client" {
      driver = "raw_exec"
      resources {
        cpu    = 1000
        memory = 300
      }

      config {
        command = "bash.exe"
        args    = [
          "-c",
          "cd H:/jam/arfies/go_backend && ./go-cqhttp_windows_amd64.exe"
        ]
      }
    }
  }
  # ----------------------------------- RiBOT ---------------------------------------------
  group "mirai-ribot" {
    count = 1
    network {
      port "cq-http" {
        static = 5703
      }
    }

    task "client" {
      driver = "raw_exec"
      resources {
        cpu    = 1000
        memory = 300
      }

      config {
        command = "bash.exe"
        args    = [
          "-c",
          "cd H:/jam/ribot/go_backend && ./go-cqhttp_windows_amd64.exe"
        ]
      }
    }
  }
}