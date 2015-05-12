#/bin/bash -eux

iconset_dir=PhotoFlow.iconset

rm -r $iconset_dir
mkdir $iconset_dir

# Create all image sizes

sips -z  16  16 "$1" --out "$iconset_dir/icon_16x16.png"
sips -z  32  32 "$1" --out "$iconset_dir/icon_32x32.png"
sips -z  64  64 "$1" --out "$iconset_dir/icon_64x64.png"
sips -z 128 128 "$1" --out "$iconset_dir/icon_128x128.png"
sips -z 256 256 "$1" --out "$iconset_dir/icon_256x256.png"

sips -z  32  32 "$1" --out "$iconset_dir/icon_16x16@2x.png"
sips -z  64  64 "$1" --out "$iconset_dir/icon_32x32@2x.png"
sips -z 128 128 "$1" --out "$iconset_dir/icon_64x64@2x.png"
sips -z 256 256 "$1" --out "$iconset_dir/icon_128x128@2x.png"
sips -z 512 512 "$1" --out "$iconset_dir/icon_256x256@2x.png"

# Create icns
iconutil -c icns --output PhotoFlow.icns PhotoFlow.iconset
rm -r PhotoFlow.iconset
