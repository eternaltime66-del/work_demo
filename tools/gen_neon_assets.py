#!/usr/bin/env python3
"""Generate deterministic Neon Expedition artwork for database-driven content."""
from __future__ import annotations

import hashlib, math, os, subprocess
from pathlib import Path
from PIL import Image, ImageDraw, ImageFilter

ROOT = Path(__file__).resolve().parents[1]
ART = ROOT / "src/main/resources/static/app/art"
MYSQL = Path(r"C:\Program Files\MySQL\MySQL Server 8.0\bin\mysql.exe")
PALETTES = [
    ("#50e6c4", "#3a8cff"), ("#4ddcff", "#9c62ff"), ("#62f4ff", "#ff4fb4"),
    ("#5ad7ff", "#ffd35c"), ("#44e0cb", "#ff785d"), ("#ed9b58", "#43d6ff"),
    ("#ff704d", "#ffcf57"), ("#926cff", "#42e7db"), ("#40d9ff", "#ff496f"),
    ("#ff8a46", "#ff3b74"), ("#6ce7ff", "#df65ff"), ("#6dd4ff", "#8b75ff"),
    ("#eef8ff", "#58d8ff"), ("#58cfff", "#d5f6ff"), ("#82eaff", "#e453ff"),
    ("#9eeaff", "#ffda7a"), ("#c5d7ff", "#a47cff"), ("#91c8ff", "#fc72cf"),
    ("#d7efff", "#63d7ff"), ("#f3f6ff", "#b06cff"),
]
TYPE_SHAPE = {"MATERIAL":0,"WEAPON":1,"ARMOR":2,"HELMET":3,"GLOVES":4,"LEGS":5,"ACCESSORY":6,"SKILL_STONE":7}

def rgb(c):
    c=c.lstrip('#'); return tuple(int(c[i:i+2],16) for i in (0,2,4))
def seed(s): return int(hashlib.sha256(s.encode()).hexdigest()[:8],16)
def chapter(code):
    try: return max(1,min(20,int(code.split('_')[1])))
    except Exception: return 1
def glow(base, layer, radius=10):
    base.alpha_composite(layer.filter(ImageFilter.GaussianBlur(radius))); base.alpha_composite(layer)
def poly(draw, pts, fill, outline=None, width=1): draw.polygon(pts,fill=fill); outline and draw.line(pts+[pts[0]],fill=outline,width=width,joint='curve')

def icon(item_id, code, name, typ):
    n=seed(code); c1,c2=map(rgb,PALETTES[chapter(code)-1]); S=256
    im=Image.new('RGBA',(S,S)); aura=Image.new('RGBA',(S,S)); a=ImageDraw.Draw(aura)
    a.ellipse((38,38,218,218),fill=(*c1,34)); glow(im,aura,20)
    d=ImageDraw.Draw(im); metal=(13,22,43,255); edge=(77,91,126,255); bright=(*c1,255); accent=(*c2,255)
    k=TYPE_SHAPE.get(typ,0); jitter=(n%17)-8
    if k==0:
        sides=5+n%4; pts=[]
        for i in range(sides):
            ang=-math.pi/2+i*2*math.pi/sides; r=70 if i%2==0 else 55
            pts.append((128+math.cos(ang)*r,128+math.sin(ang)*r))
        poly(d,pts,metal,bright,6); d.ellipse((91,91,165,165),fill=(*c2,110),outline=accent,width=5); d.line((75,158,181,98),fill=bright,width=5)
    elif k==1:
        if 'gun' in code or 'core' in code and n%2:
            d.rounded_rectangle((34,105,192,155),12,fill=metal,outline=bright,width=6); d.polygon([(166,153),(220,153),(220,171),(160,171)],fill=metal,outline=edge); d.rectangle((75,150,108,207),fill=metal,outline=accent,width=5)
        else:
            poly(d,[(42,190),(82,150),(166,48),(210,40),(198,87),(94,178)],metal,bright,6); d.rounded_rectangle((48,174,112,205),8,fill=metal,outline=accent,width=5)
    elif k==2:
        poly(d,[(65,64),(108,42),(128,68),(148,42),(191,64),(178,199),(128,218),(78,199)],metal,bright,6); d.line((128,70,128,205),fill=accent,width=6); d.arc((78,84,178,177),15,165,fill=edge,width=5)
    elif k==3:
        d.pieslice((52,45,204,205),180,360,fill=metal,outline=bright,width=6); d.rounded_rectangle((55,115,201,191),25,fill=metal,outline=edge,width=5); d.line((76,145,180,145),fill=accent,width=9)
    elif k==4:
        d.rounded_rectangle((67,81,188,198),24,fill=metal,outline=bright,width=6)
        for i in range(4): d.rounded_rectangle((59+i*31,43+jitter//3,82+i*31,115),9,fill=metal,outline=accent,width=4)
        d.line((87,136,169,136),fill=edge,width=7)
    elif k==5:
        for x in (73,133):
            d.rounded_rectangle((x,57,x+50,185),15,fill=metal,outline=bright,width=6); d.polygon([(x-8,174),(x+54,174),(x+64,212),(x-15,212)],fill=metal,outline=accent)
    elif k==6:
        d.ellipse((48,48,208,208),fill=metal,outline=bright,width=7); d.ellipse((76,76,180,180),outline=accent,width=8); d.ellipse((108,108,148,148),fill=(*c2,180),outline=(240,248,255,255),width=4); d.arc((38,38,218,218),20+n%40,220+n%70,fill=edge,width=5)
    else:
        pts=[(128,32),(189,91),(171,180),(128,224),(85,180),(67,91)]
        poly(d,pts,(*c2,180),bright,7); poly(d,[(128,49),(154,102),(128,179),(102,102)],(*c1,150),(235,245,255,255),4); d.ellipse((113,108,143,138),fill=(245,250,255,220))
    # deterministic circuitry makes same-slot equipment chapter-specific
    for i in range(3):
        y=72+((n>>(i*4))%112); d.line((56,y,76+(n%64),y),fill=(*c2,150),width=3)
    out=ART/'item'/f'{code}.png'; out.parent.mkdir(parents=True,exist_ok=True); im.save(out,optimize=True)

def monster(mid,name,rarity):
    nn=chapter(mid.replace('MST_N20_','n20_')); idx=int(mid.rsplit('_',1)[-1]); c1,c2=map(rgb,PALETTES[nn-1]); n=seed(mid)
    idle_im=None
    for pose in ('idle_4','attack','hit'):
        S=420; im=Image.new('RGBA',(S,S)); aura=Image.new('RGBA',(S,S)); a=ImageDraw.Draw(aura)
        a.ellipse((70,90,350,360),fill=(*c1,32)); glow(im,aura,20); d=ImageDraw.Draw(im)
        metal=(10,18,37,255); plate=(26,38,67,255); dx=18 if pose=='attack' else (-10 if pose=='hit' else 0); scale=1.25 if rarity=='BOSS' else (1.05 if rarity=='RARE' else .86)
        cx=210+dx; cy=220; rw=105*scale; rh=95*scale
        # silhouette families: insect, service machine, elite humanoid, guardian
        if idx==1:
            d.ellipse((cx-rw,cy-rh,cx+rw,cy+rh),fill=metal,outline=(*c1,255),width=7)
            for side in (-1,1):
                for j in range(3): d.line((cx+side*55,cy-20+j*35,cx+side*(120+j*12),cy-65+j*58),fill=plate,width=16)
            d.ellipse((cx-37,cy-32,cx+37,cy+34),fill=(*c2,155),outline=(230,250,255,255),width=5)
        elif idx==2:
            d.rounded_rectangle((cx-rw,cy-rh,cx+rw,cy+rh),26,fill=metal,outline=(*c1,255),width=7)
            for x in (-58,58): d.rounded_rectangle((cx+x-28,cy+70,cx+x+28,cy+157),14,fill=plate,outline=(*c2,255),width=5)
            d.ellipse((cx-52,cy-45,cx+52,cy+55),fill=plate,outline=(*c2,255),width=6)
        elif idx==3:
            d.ellipse((cx-72*scale,cy-142*scale,cx+72*scale,cy-15*scale),fill=metal,outline=(*c1,255),width=7)
            poly(d,[(cx-105*scale,cy-12*scale),(cx+105*scale,cy-12*scale),(cx+80*scale,cy+125*scale),(cx-80*scale,cy+125*scale)],metal,(*c2,255),7)
            d.line((cx-44,cy-80,cx+44,cy-80),fill=(*c2,255),width=12)
        else:
            d.rounded_rectangle((cx-rw,cy-rh,cx+rw,cy+rh),35,fill=metal,outline=(*c1,255),width=9)
            for side in (-1,1):
                d.ellipse((cx+side*rw-side*35-42,cy-35,cx+side*rw-side*35+42,cy+49),fill=plate,outline=(*c2,255),width=7)
                d.rounded_rectangle((cx+side*rw-side*27-34,cy+34,cx+side*rw-side*27+34,cy+145),18,fill=metal,outline=(*c1,255),width=6)
            d.ellipse((cx-52,cy-52,cx+52,cy+52),fill=(*c2,170),outline=(240,250,255,255),width=7)
        if pose=='hit': d.polygon([(284,70),(260,136),(298,126),(272,190)],fill=(255,83,108,230))
        if pose=='attack': d.arc((60,50,370,360),290,55,fill=(*c2,240),width=13)
        if pose=='idle_4': idle_im=im.copy()
        out=ART/'char'/mid/f'{pose}.png'; out.parent.mkdir(parents=True,exist_ok=True); im.save(out,optimize=True)
    # portrait crop is independently framed for readability
    face=idle_im.crop((70,48,350,328)).resize((192,192),Image.Resampling.LANCZOS)
    for folder,suffix in ((ART/'face',f'{mid}.png'),(ART/'monster',f'{mid}-face.png')):
        folder.mkdir(parents=True,exist_ok=True); face.save(folder/suffix,optimize=True)

def chapter_art(cid,name,remark,no):
    W,H=720,360; c1,c2=map(rgb,PALETTES[no-1]); im=Image.new('RGB',(W,H),(5,11,27)); d=ImageDraw.Draw(im)
    for y in range(H):
        t=y/H; d.line((0,y,W,y),fill=tuple(int((1-t)*a+t*b) for a,b in zip((6,13,32),(14,21,43))))
    horizon=230-(no%4)*10
    for i in range(18):
        x=(i*53+seed(cid)%47)%W; h=65+(seed(cid+str(i))%150); w=25+(i%4)*12
        d.rectangle((x,horizon-h,x+w,horizon),fill=(10,22,43),outline=(*c1,120),width=2)
        for yy in range(horizon-h+12,horizon-8,18): d.line((x+5,yy,x+w-5,yy),fill=(*c2,105),width=2)
    d.line((0,horizon,W,horizon),fill=(*c1,220),width=4)
    for i in range(8): d.line((W//2,horizon,W//2+(i-4)*150,H),fill=(*c2,70),width=2)
    mist=Image.new('RGBA',(W,H)); md=ImageDraw.Draw(mist)
    for i in range(9): md.ellipse((i*90-100,220+(i%3)*18,i*90+190,390),fill=(*c1,18))
    im=Image.alpha_composite(im.convert('RGBA'),mist.filter(ImageFilter.GaussianBlur(22))).convert('RGB')
    out=ART/'chapter'/f'{cid}.jpg'; out.parent.mkdir(parents=True,exist_ok=True); im.save(out,quality=91,optimize=True)

def ui_assets():
    """Fill static UI art slots with the same restrained neon language."""
    # reusable transparent symbols
    specs={'hud/coin.png':'coin','hud/energy.png':'energy','empty/bag.png':'bag','empty/level.png':'level','empty/recipe.png':'recipe'}
    for rel,kind in specs.items():
        im=Image.new('RGBA',(192,192)); d=ImageDraw.Draw(im); cyan=(73,224,255,255); mag=(219,79,255,255); metal=(18,29,55,255)
        if kind=='coin': d.ellipse((40,40,152,152),fill=metal,outline=(255,211,79,255),width=10); d.ellipse((68,68,124,124),outline=(255,211,79,255),width=8)
        elif kind=='energy': poly(d,[(110,19),(49,107),(92,107),(73,173),(145,80),(102,80)],metal,cyan,9)
        elif kind=='bag': d.rounded_rectangle((39,65,153,159),18,fill=metal,outline=cyan,width=8); d.arc((66,28,126,94),180,360,fill=mag,width=8)
        elif kind=='level': d.rounded_rectangle((36,37,156,155),18,fill=metal,outline=cyan,width=8); d.line((61,128,91,88,115,106,142,65),fill=mag,width=9)
        else: d.ellipse((43,43,149,149),fill=metal,outline=mag,width=8); d.line((68,125,124,69),fill=cyan,width=10); d.ellipse((91,91,116,116),fill=(235,250,255,255))
        out=ART/rel; out.parent.mkdir(parents=True,exist_ok=True); im.save(out,optimize=True)
    # battle primitives
    for rel,color,box in [('ui/unit-shadow.png',(21,35,62,150),(20,70,492,185)),('ui/aura-rare.png',(102,213,255,110),(28,28,228,228)),('ui/clash-line.png',(115,220,255,210),(0,27,512,37)),('home/shadow.png',(18,28,55,145),(20,60,492,172))]:
        size=(512,200) if 'shadow' in rel or 'clash' in rel else (256,256); im=Image.new('RGBA',size); d=ImageDraw.Draw(im)
        if 'aura' in rel: d.ellipse(box,outline=color,width=9); d.ellipse((48,48,208,208),outline=(210,79,255,85),width=6)
        elif 'clash' in rel: d.rounded_rectangle(box,8,fill=color)
        else: d.ellipse(box,fill=color)
        out=ART/rel; out.parent.mkdir(parents=True,exist_ok=True); im.save(out,optimize=True)
    # home and craft environments
    chapter_art('stage-bg','', '', 15); (ART/'chapter/stage-bg.jpg').replace(ART/'home/stage-bg.jpg')
    chapter_art('anvil-bg','', '', 7); (ART/'chapter/anvil-bg.jpg').replace(ART/'craft/anvil-bg.jpg')
    mid=Image.new('RGBA',(720,300)); md=ImageDraw.Draw(mid); md.line((0,238,720,238),fill=(73,224,255,150),width=4)
    for x in range(40,720,90): md.rectangle((x,135,x+45,238),fill=(13,24,47,230),outline=(214,75,255,125),width=3)
    (ART/'home').mkdir(parents=True,exist_ok=True); mid.save(ART/'home/stage-mid.png',optimize=True)
    # vector slot silhouettes remain sharp at every equipment-layout size
    slot_shapes={'weapon':'M25 82L77 18 91 12 85 30 39 91Z','armor':'M22 27L42 14 50 27 58 14 78 27 70 88 50 96 30 88Z','helmet':'M20 72A30 30 0 0 1 80 72V86H20Z','gloves':'M30 28H70V84H30Z','legs':'M27 18H47V80H18L25 48ZM53 18H73L75 48 82 80H53Z','acc':'M50 14A36 36 0 1 1 49.9 14ZM50 34A16 16 0 1 0 50 66A16 16 0 1 0 50 34Z'}
    (ART/'equip').mkdir(parents=True,exist_ok=True)
    for key,pathdata in slot_shapes.items():
        svg=f'''<svg xmlns="http://www.w3.org/2000/svg" viewBox="0 0 100 100"><defs><filter id="g"><feGaussianBlur stdDeviation="2" result="b"/><feMerge><feMergeNode in="b"/><feMergeNode in="SourceGraphic"/></feMerge></filter></defs><path d="{pathdata}" fill="#101d38" stroke="#50e6ff" stroke-width="4" filter="url(#g)"/></svg>'''
        (ART/'equip'/f'slot-{key}.svg').write_text(svg,encoding='utf-8')

def query(sql):
    cmd=[str(MYSQL),'--host=127.0.0.1','--user=root','--password=123456','--default-character-set=utf8mb4','--batch','--skip-column-names','--database=game3',f'--execute={sql}']
    p=subprocess.run(cmd,capture_output=True,text=True,encoding='utf-8'); p.check_returncode(); return [x.split('\t') for x in p.stdout.splitlines() if x]

def main():
    items=query("SELECT id,code,name,item_type FROM app_item WHERE enable=1 ORDER BY sort,id")
    monsters=query("SELECT id,name,rarity FROM app_monster WHERE id LIKE 'MST_N20_%' ORDER BY sort,id")
    chapters=query("SELECT id,name,COALESCE(remark,''),sort FROM app_stage WHERE id LIKE 'SCP_N20_%' ORDER BY sort")
    for row in items: icon(*row)
    for row in monsters: monster(*row)
    for cid,name,remark,no in chapters: chapter_art(cid,name,remark,int(no))
    ui_assets()
    sql="UPDATE app_item SET icon=CONCAT('art/item/',code,'.png') WHERE enable=1 AND (icon IS NULL OR icon='');"
    query(sql)
    print(f"generated items={len(items)}, monsters={len(monsters)*5}, chapters={len(chapters)}")

if __name__=='__main__': main()
