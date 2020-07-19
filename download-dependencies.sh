#create new directory if it doesn't already exist
mkdir -p dependencies

#download files
wget "https://github.com/jMonkeyEngine/jmonkeyengine/releases/download/v3.3.2-stable/jME3.3.2-stable.zip" -O "dependencies/jME3.3.2-stable.zip"
wget "https://algs4.cs.princeton.edu/code/algs4.jar" -O "dependencies/algs4.jar"
wget "https://upload.wikimedia.org/wikipedia/commons/1/1f/Cosmic_%E2%80%98Winter%E2%80%99_Wonderland.jpg" -O "assets/Pictures/Cosmic Winter Wonderland.jpg"

#prepare to check against MD5 checksums
FILES=(dependencies/jME3.3.2-stable.zip dependencies/algs4.jar "assets/Pictures/Cosmic Winter Wonderland.jpg")
CKSUMS=(3bbb9534037ba3712d8e2219ebfe5faf 762c51d2444097217fbf027569748d30 8121ffc464101a9a26a2e05f817a6837)
MD5=$(command -v md5 || command -v md5sum)

#check against checksums
for((i = 0; i < ${#FILES[@]}; i++)); do
    file=${FILES[$i]}
    echo "file is $file"
    CHECKSUM=$($MD5 "$file")
    
    if [[ "$CHECKSUM" != *"${CKSUMS[i]}"* ]]; then #if CHECKSUM doesn't contain the ith checksum
        echo "The checksum for $file is wrong, so either the download failed or the checksum is outdated."
        exit 1
    fi
done

#unzip the zip
unzip dependencies/jME3.3.2-stable.zip -d "dependencies/jME3.3.2-stable/" && rm dependencies/jME3.3.2-stable.zip