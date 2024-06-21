{ pkgs,
  nixpkgs,
  opengen,
  system,
  uber,
  pname,
  basicToolsFn,
  depsCache,
}: let
    # in OCI context, whatever our host platform we want to build same arch but linux
    systemWithLinux = builtins.replaceStrings [ "darwin" ] [ "linux" ] system;

    crossPkgsLinux = nixpkgs.legacyPackages.${systemWithLinux};

    # TODO: This can be factored out into an gensql/nix
    # shared package.
    baseImg = opengen.packages.${system}.ociImgBase;

    ociBin = crossPkgsLinux.callPackage ./../bin {inherit uber pname;};
in pkgs.dockerTools.buildLayeredImage {
  name = "probcomp/gensql.query";
  tag = systemWithLinux;
  fromImage = baseImg;
  # architecture
  contents = [ ociBin depsCache crossPkgsLinux.clojure ];
  config = {
    Cmd = [ "${ociBin}/bin/${pname}" ];
    Env = [
      "CLJ_CONFIG=${depsCache}/.clojure"
      "GITLIBS=${depsCache}/.gitlibs"
      "JAVA_TOOL_OPTIONS=-Duser.home=${depsCache}"
    ];
  };
}



