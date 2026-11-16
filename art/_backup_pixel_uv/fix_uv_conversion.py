import json,sys
base='.'
blocks=['tripod','projector','darkroom_enlarger','darkroom_safelight','developer_tray','developer_station','film_shelf','screen']
items=['analog_camera','film_roll','film_cartridge','lens','clapperboard']
def el(m,name):
    for e in m['elements']:
        if e.get('name')==name: return e
    print('  !! element not found',name); return None
def patch(kind,n,m):
    if n=='projector': m['render_type']='minecraft:cutout'
    if n=='darkroom_safelight':
        h=el(m,'housing')
        if h and 'north' in h['faces'] and 'south' in h['faces']:
            a,b=h['faces']['north']['uv'],h['faces']['south']['uv']
            h['faces']['north']['uv'],h['faces']['south']['uv']=b,a
    if n=='analog_camera':
        v=el(m,'viewfinder')
        if v: v['faces'].pop('north',None)
        f=el(m,'film_window')
        if f: f['from'][0]=1.49; f['to'][0]=1.49
    if n=='lens':
        g=el(m,'front_glass')
        if g: g['to'][2]=3
        gr=el(m,'grip')
        if gr: gr['from'][2]=3.01
for kind,names in (('block',blocks),('item',items)):
    for n in names:
        p=f'{kind}/{n}.json'
        m=json.load(open(p))
        patch(kind,n,m)
        ts=m.get('texture_size',[16,16])
        sx,sy=16/ts[0],16/ts[1]
        for e in m['elements']:
            for f in e['faces'].values():
                if 'uv' in f:
                    u=f['uv']; f['uv']=[round(u[0]*sx,4),round(u[1]*sy,4),round(u[2]*sx,4),round(u[3]*sy,4)]
        json.dump(m,open(p,'w'),indent=2)
        print('ok',p,ts)
