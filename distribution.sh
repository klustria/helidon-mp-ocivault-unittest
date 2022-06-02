#!/bin/bash

package() {
  local package_name="helidon-mp-oci-unittest-$1"
  cp -r "$1" "$package_name"
  zip -r "$package_name".zip "$package_name"
  rm -rf "$package_name"
}

# Display usage information for this tool.
displayHelp() {
  echo "Usage: $(basename "$0") {complete|practice|both} [start Option] "
  echo
  echo "   complete   creates a complete zip distribution of the Helidon MP project which will be used as reference after the practice"
  echo "   practice   creates a zip distribution of the Helidon MP project without the test which will be used for the practice"
  echo "   both       creates both complete and practice zip distributions"
  echo
}

# Main routine
case "$1" in
  complete | practice)
    package "$1"
    ;;
  both)
    package "complete" && package "practice"
    ;;
  *)
    displayHelp
    ;;
esac