from PIL import Image, ImageDraw, ImageFont
from pathlib import Path
import glob, xml.etree.ElementTree as ET
root=Path("extracted")
res=root/"app/src/main/res"
for size,folder in [(48,"mipmap-mdpi"),(72,"mipmap-hdpi"),(96,"mipmap-xhdpi"),(144,"mipmap-xxhdpi"),(192,"mipmap-xxxhdpi")]:
    scale=4
    S=size*scale
    im=Image.new("RGB",(S,S),(11,21,39))
    d=ImageDraw.Draw(im)
    gold=(250,194,65)
    d.rounded_rectangle((S*.28,S*.20,S*.72,S*.59),radius=int(S*.09),fill=gold)
    d.arc((S*.15,S*.23,S*.42,S*.55),90,270,fill=gold,width=int(S*.055))
    d.arc((S*.58,S*.23,S*.85,S*.55),270,90,fill=gold,width=int(S*.055))
    d.rectangle((S*.47,S*.57,S*.53,S*.73),fill=gold)
    d.rounded_rectangle((S*.32,S*.72,S*.68,S*.79),radius=int(S*.025),fill=gold)
    d.polygon([(S*.5,S*.28),(S*.54,S*.38),(S*.65,S*.39),(S*.57,S*.46),(S*.60,S*.56),(S*.5,S*.50),(S*.40,S*.56),(S*.43,S*.46),(S*.35,S*.39),(S*.46,S*.38)],fill=(255,246,197))
    target=res/folder
    target.mkdir(parents=True,exist_ok=True)
    im.resize((size,size),Image.Resampling.LANCZOS).save(target/"ic_wintracker.png")
manifest=next(root.rglob("app/src/main/AndroidManifest.xml"))
ET.register_namespace("android","http://schemas.android.com/apk/res/android")
tree=ET.parse(manifest)
app=tree.getroot().find("application")
app.set("{http://schemas.android.com/apk/res/android}icon","@mipmap/ic_wintracker")
app.set("{http://schemas.android.com/apk/res/android}roundIcon","@mipmap/ic_wintracker")
tree.write(manifest,encoding="utf-8",xml_declaration=True)
print("WinTracker launcher icon generated and applied")
