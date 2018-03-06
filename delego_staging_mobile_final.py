from flask import Flask, render_template, url_for, request, session, redirect, send_from_directory
from flask_pymongo import PyMongo
import json
from bson.json_util import dumps
import bcrypt
import random
import os
from signal import signal, SIGPIPE, SIG_DFL
from werkzeug.utils import secure_filename
import subprocess
import datetime

APP_ROOT = os.path.dirname(os.path.abspath(__file__))
UPLOAD_FOLDER = os.path.join(APP_ROOT, 'static/uploads')
THUMBS_FOLDER = os.path.join(APP_ROOT, 'static/thumbs')
ALLOWED_EXTENSIONS = {'txt', 'pdf', 'doc', 'docx', 'jpg'}

app = Flask(__name__)
app.config['MONGO_DBNAME'] = 'delego'
app.config['MONGO_URI'] = 'mongodb://localhost:27017/delego'
app.config['UPLOAD_FOLDER'] = UPLOAD_FOLDER
app.config['THUMBS_FOLDER'] = THUMBS_FOLDER
mongo = PyMongo(app)

session_timings = { 'session1' : { 'start' : '2018-02-23T14:00:00Z', 'end' : '2018-02-23T16:15:00Z'}, 'session2' : { 'start' : '2018-02-23T16:30:00Z', 'end' : '2018-02-23T18:30:00Z'}, 'session3' : { 'start' : '2018-02-24T10:00:00Z', 'end' : '2018-02-24T13:30:00Z' }, 'session4' : { 'start' : '2018-02-24T14:00:00Z', 'end' : '2018-02-24T16:15:00Z'}, 'session5' : { 'start' : '2018-02-24T16:30:00Z', 'end' : '2018-02-24T18:30:00Z'}, 'session6' : { 'start' : '2018-02-25T10:00:00Z', 'end' : '2018-02-25T13:30:00Z'}, 'session7' : { 'start' : '2018-02-25T14:00:00Z', 'end' : '2018-02-25T16:15:00Z'} }

def allowed_file(filename):
    print(filename)
    return '.' in filename and \
           filename.rsplit('.', 1)[1].lower() in ALLOWED_EXTENSIONS

@app.route('/mobile/search_delegate/<query>', methods=['POST', 'GET'])
def search_delegate(query):
    committees = mongo.db.mundb
    find_users = committees.find({'name' : {'$regex' : query, '$options' : 'i'}})
    return dumps(find_users)

@app.route('/mobile/current_session')
def mobilecurrent_session():
    sessionflag = 0
    today = datetime.datetime.now().isoformat()
    for i in range(len(session_timings)):
        temp = 'session' + str(i+1)
        if session_timings[temp]['start'] <= today and session_timings[temp]['end'] > today:
            sessionflag = 1
            return json.dumps({'session' : temp})
    if sessionflag == 0:
        return json.dumps({'session' : 'No Session currently active'})

@app.route('/mobile/current_attendance/<user_id>&<session_id>')
def mobilecheck_attendance(user_id, session_id):
    committees = mongo.db.mundb
    hello = committees.find_one({'identifier' : user_id})
    current_session = hello['attendance']
    current_s = current_session[str(session_id)]
    return json.dumps({'attendance' : current_s})

@app.route('/mobile/set_attendance/<user_id>&<type_att>&<session_id>')
def mobileattendance(user_id, type_att, session_id):
    committees = mongo.db.mundb
    query = 'attendance.'+session_id
    hello = committees.find_one({'identifier' : user_id})
    test = hello['attendance']
    committees.update_one({'identifier':user_id}, {"$set" : {'attendance.'+str(session_id) : str(type_att)}})
    hello = committees.find_one({'identifier' : user_id})
    test = hello['attendance']
    # print "After update : Test : " + str(test)
    return json.dumps({'status' : 'success'}) #{'set_attendance' : type_att, 'set_session' : session_id}'')

@app.route('/mobile/user_details/<user_id>')
def mobileuser_details(user_id):
    committees = mongo.db.mundb
    find_user = committees.find_one({'identifier' : str(user_id)})
    if (bcrypt.hashpw(find_user['name'].encode('utf-8'), user_id.encode('utf-8')) == find_user['identifier'].encode('utf-8')):
        user_name = find_user['name']
        user_country = find_user['country']
        user_committee = find_user['committee']
        user_role = find_user['role']
        #user_portfolios = find_user['portfolio']
        user_rsvp = find_user['rsvp']
        user_image = find_user['image']
        user_numid = find_user['numid']
        user_email = find_user['email']
        user_phonenumber = find_user['number']
        user_attendance = find_user['attendance']
        user_background = find_user['background']
        user_formals = find_user['formals']
        user_informals = find_user['informals']
        """user_name = find_user['name']`
        user_country = find_user['country']
        user_committee = find_user['committee']
        user_role = find_user['role']
        user_portfolios = find_user['portfolio']
        user_rsvp = find_user['rsvp']
        user_image = find_user['image']
        user_numid = find_user['numid']"""
        return json.dumps({'background': user_background, 'formals':user_formals, 'informals' : user_informals, 'Name': user_name, 'Country' : user_country, 'Committee' : user_committee, 'Numid':str(user_numid), 'attendance':user_attendance,'Role': user_role,  'RSVP' : user_rsvp, 'Image' : user_image, 'Email' : user_email, 'Phone' : str(user_phonenumber), 'identifier' : user_id})
    else:
        return json.dumps({'Name' : 'Authentication Token failed.', 'Country' : 'Authentication Token failed', 'Committee' : 'Authentication Token failed', 'Numid': 'Authentication Token failed', 'Role': 'Authentication Token failed', 'Portfolios':'Authentication Token failed', 'RSVP' : 'Authentication Token failed', 'Image' : 'Authentication Token failed'})

@app.route('/mobile/user_arrival/<user_id>')
def mobileuser_arrival(user_id):
    committees = mongo.db.mundb
    committees.update_one({'identifier': user_id}, {"$set": {'rsvp': 'Arrived'}})
    return dumps('success')#{'set_arrival' : user_id})

@app.route('/mobile/allusers', methods=['POST', 'GET'])
def mobileallusers():
    committees = mongo.db.mundb
    users = mongo.db.users
    all_users = committees.find({}, {'_id': False}).sort("name", 1)
    return dumps(all_users)

@app.route('/mobile/user_sort/<user_type>', methods=['POST', 'GET'])
def mobileuser_sort(user_type):
    committees = mongo.db.mundb

    if (user_type == 'All'):
        find_users = committees.find({}, {'_id': False})
        return dumps({'delegate': find_users})
    else:
        find_users = committees.find({'committee' : user_type}, {'_id': False})
        return dumps({'delegate': find_users})

@app.route('/mobile/search_delegate/<query>', methods=['POST', 'GET'])
def mobilesearch_delegate(query):
    committees = mongo.db.mundb
    users = mongo.db.users
    find_users = committees.find({'name' : {'$regex' : query, '$options' : 'i'}})
    return dumps({'delegate': find_users})

@app.route('/mobile/all_names', methods=['POST', 'GET'])
def mobileall_names():
    committees = mongo.db.mundb
    users = mongo.db.users
    find_users = committees.find({}, {'name':True, '_id': False})
    return dumps(find_users)

@app.route('/mobile/by_committee/<committee_name>', methods=['POST', 'GET'])
def mobileby_committee(committee_name):
    committees = mongo.db.mundb
    find_users = committees.find({'committee' : committee_name}, {'_id' : False, 'portfolio':False, 'role':False}).sort("country", 1)#'Name' : True, 'Country' : True, 'Role' : True, 'Committee' : True})
    return dumps(find_users)

@app.route('/mobile/committee_status/<committee_name>', methods=['POST', 'GET'])
def mobilestatus_committee(committee_name):
    committees = mongo.db.mundb
    find_arrived = committees.find({'committee' : committee_name, 'rsvp' : 'Arrived'}).count()
    find_pending = committees.find({'committee' : committee_name, 'rsvp' : 'Pending'}).count()
    total_count = find_arrived + find_pending
    return json.dumps({'arrived' : find_arrived, 'pending' : find_pending, 'total' : total_count})

@app.route('/mobile/login', methods=['POST', 'GET'])
def mobilelogin():
    print("Username : " + str(request.form['username']) + "     PAssword : " + str(request.form['password']))
    global login_flag
    users = mongo.db.users
    login_user = users.find_one({'email' : request.form['username']})
    if login_user is None:
       print("Returning invalid email")
       return json.dumps({'login' : 'successful'})
    print(login_user)
    login_username = login_user['name']
    login_fullname = login_user['name']
    login_mail = login_user['email']
    login_user_type = login_user['user_type']
    login_committee = login_user['committee']
    login_identifier = login_user['identifier']
    login_background = login_user['background']
    randint = random.randint(100000,999999)
    #data = str(request.values)
    #print data
    auth_string = '%s%s' % (request.form['username'], randint)
    #auth_token = bcrypt.hashpw(request.form['username'].encode('utf-8'), bcrypt.gensalt())
    #print login_user
    if login_user:
       print("Inside login_user")
       if bcrypt.hashpw(request.form['password'].encode('utf-8'), login_user['password'].encode('utf-8'))==login_user['password'].encode('utf-8'):
            print("Inside bcrypt")
            auth_token = str(bcrypt.hashpw(auth_string.encode('utf-8'), bcrypt.gensalt()))
            print("Auth token :  " +  str(auth_token))
            login_flag = 1#session['username'] = request.form['username']
            return json.dumps({'login_background' : login_background, 'login_identifier' : login_identifier, 'login_committee' : login_committee, 'auth_token' : auth_token, 'login' : 'success', 'email':login_mail, 'username':login_username, 'fullname' : login_fullname, 'type':login_user_type})#return redirect(url_for('index'))
    return json.dumps({'login': 'unsuccessful'})

@app.route('/mobile/checkfeedback/<user_id>')
def mobilecheckfeedback(user_id):
    today = datetime.datetime.now().isoformat()
    find_feedback = mongo.db.feedback.find_one({'identifier' : user_id, 'date' : today[:10]})
    if find_feedback is None:
        return json.dumps({'status' : 'Not Set'})
    else:
        return json.dumps({'status' : 'Already submitted'})
@app.route('/mobile/submitfeedback', methods=['POST'])
def mobilesubmitfeedback():
    user_id = request.form['user_id']
    today = datetime.datetime.now().isoformat()
    find_user = mongo.db.mundb.find_one({ 'identifier' : user_id })
    mongo.db.feedback.insert_one({'name' : find_user['name'], 'date' : today[:10], 'identifier' : find_user['identifier'], 'feedback' : { 'Food & beverages' : request.form['rating'], 'Organisational Logistics' : request.form['2-rating'], 'Debate Quality' : request.form['3-rating'], 'EB Moderation Quality' : request.form['4-rating'], 'Accomodation Facility' : request.form['5-rating']}})
    return dumps('success') # set flag to ensure that the feedback form can be filled only once during the day. If you want me to give you a method to check if feedback for a user_id already exists, let me know. will do that.

#Ignore the above comment. already gave you a method to check existing feedback.

@app.route('/mobile/getformals/<user_id>')
def mobilegetformals(user_id):
    users = mongo.db.mundb
    find_user = users.find_one({'identifier': user_id})
    formalflag = find_user['formal_flag']
    if formalflag == 0:
        return json.dumps({'status' : 'Not Set'})
    else:
        return json.dumps({'status' : 'You have already submitted your response'})

@app.route('/mobile/formals/<user_id>&<type_att>')
def mobileformals(user_id, type_att):
    committees = mongo.db.mundb
    committees.update_one({'identifier':user_id}, {"$set" : {'formals': type_att, 'formal_flag' : 1}})
    return dumps('success')#{'set_formal' : type_att})

@app.route('/mobile/getinformals/<user_id>')
def mobilegetinformals(user_id):
    users = mongo.db.mundb
    find_user = users.find_one({'identifier':user_id})
    informalflag = find_user['informal_flag']
    if informalflag == 0:
        return json.dumps({'status' : 'Not Set'})
    else:
        return json.dumps({'status' : 'You have already submitted your response'})

@app.route('/mobile/informals/<user_id>&<type_att>')
def mobileinformals(user_id, type_att):
    committees = mongo.db.mundb
    committees.update_one({'identifier':user_id}, {"$set" : {'informals': type_att, 'informal_flag' : 1}})
    return dumps('success')#{'set_informal' : type_att})

@app.route('/mobile/getlunch/<user_id>')
def mobilegetlunch(user_id):
    users = mongo.db.mundb
    find_user = users.find_one({'identifier': user_id})
    lunchflag = find_user['lunch_flag'] #print "Hello from Informals"
    if lunchflag == 0:
        return json.dumps({'status' : 'Not Set'})
    else:
        return json.dumps({'status': 'You have already submitted your response'})

@app.route('/mobile/lunch/<user_id>&<type_att>')
def mobilelunch(user_id,type_att):
    committees = mongo.db.mundb
    committees.update_one({'identifier':user_id}, {"$set" : {'lunch': type_att, 'lunch_flag' : 1}})
    return dumps('success')

@app.route('/mobile/getbreakfast/<user_id>')
def mobilegetbreakfast(user_id):
    users = mongo.db.mundb
    find_user = users.find_one({'identifier': user_id})
    breakfastflag = find_user['breakfast_flag'] #print "Hello from Informals"
    if breakfastflag == 0:
        return json.dumps({'status' : 'Not Set'})
    else:
        return json.dumps({'status' : 'You have already submitted your response'})

@app.route('/mobile/breakfast/<user_id>&<type_att>')
def mobilebreakfast(user_id,type_att):
    committees = mongo.db.mundb
    committees.update_one({'identifier':user_id}, {"$set" : {'breakfast': type_att, 'breakfast_flag' : 1}})
    return dumps('success')

@app.route('/mobile/viewuploads/<committee_name>')
def mobileviewuploads(committee_name):
    find_users = mongo.db.uploads.find({'committee' : committee_name })
    if find_users is None:
        return json.dumps({})
    return dumps(find_users)

@app.route('/mobile/fileupload', methods=['POST'])
def mobilefileupload():
        if request.method == 'POST':
                file = request.files['file']
                if file and allowed_file(file.filename):
                    users = mongo.db.mundb
                    user_identifier = request.form.get('identifier')
                    find_user = users.find_one({'identifier' : user_identifier})
                    t1 = (file.filename).split('.')
                    tempname = str(find_user['numid']) + '_' + find_user['committee'] + "_" + find_user['country']+ '_' + datetime.datetime.now().isoformat() + '.' +t1[1]
                    filename = secure_filename(tempname)
                    # print filename
                    file.save(os.path.join(app.config['UPLOAD_FOLDER'], filename))
                    fname = filename
                    # print "successfully saved : " + str(fname)
                    fname = filename.split('.')
                    today = datetime.datetime.now().isoformat()
                    thumb_link=''
                    if t1[1] == 'pdf':
                       fname_path = os.path.join(app.config['UPLOAD_FOLDER'], filename)
                       thumb_name = '%s.jpg' % fname[0]
                       print("thumb_name : " + thumb_name)
                       thumb_path = os.path.join(app.config['THUMBS_FOLDER'], thumb_name)
                       print("thumb_path : " + str(thumb_path))
                       thumb_link = '/static/thumbs/%s' % thumb_name
                       download_link = '/downloads/%s' % fname
                       generateThumbnail(fname_path, thumb_path)
                    
                    mongo.db.uploads.insert_one({'name' : find_user['name'], 'date' : today[:10], 'country' : find_user['country'], 'committee' : find_user['committee'], 'file' : filename, 'thumb' : thumb_link, 'extn' : t1[1] })
                    #users.update_one({'identifier':user_identifier}, {"$set": {'fileupload': 'true', 'extn' : t1[1]}})
                    return dumps('successful')
                    # return render_template('done.html', committee_name = session['committee'], delegate_name = session['username'])
                return dumps('File type not allowed')
        return dumps('Unknown Method Type. Use POST.')

def generateThumbnail(fname_path, thumb_path):
    page_one_only = '%s[0]' % fname_path
    thumbnail_params = ['convert', page_one_only, thumb_path]
    subprocess.Popen(thumbnail_params)

@app.route('/mobile/speakers/<committee_name>')
def mobilespeakers(committee_name):
    find_speakers = mongo.db.speakers.find_one({'committee' : committee_name})
    if find_speakers is None:
        return json.dumps({})
    temp = []
    count = len(find_speakers['speakers'])
    for i in range(count):
        temp.append({'speaker' : find_speakers['speakers'][i], 'country' : find_speakers['country'][i]})
    return dumps(temp)

@app.route('/mobile/delfromuploads/<filename>')
def mobiledelfromuploads(filename):
        # print filename
        mongo.db.uploads.remove({'file' : filename})
        return json.dumps({'status' : 'deleted'})

@app.route('/mobile/delfromspeakers/<committee_name>&<speaker_name>')
def mobiledelfromspeakers(committee_name,speaker_name):
        find_speaker = mongo.db.speakers.find_one({'committee' : committee_name})
        if speaker_name in find_speaker['speakers']:
            temp = find_speaker['speakers'].index(speaker_name)
            del find_speaker['speakers'][temp]
            del find_speaker['country'][temp]
            mongo.db.speakers.update_one({'committee' : committee_name}, {'$set' : {'speakers' : find_speaker['speakers'], 'country' : find_speaker['country']}})
            return json.dumps({'status' : 'deleted'})
        return json.dumps({'status' : 'Speaker not found'})

@app.route('/mobile/resetspeakers/<committee_name>')
def mobileresetspeakers(committee_name):
        mongo.db.speakers.remove({'committee' : committee_name})
        return json.dumps({'status' : 'deleted'})

@app.route('/mobile/addtospeakers/<user_id>&<committee>')
def mobileaddtospeakers(user_id, committee):
    print(committee)
    find_committee = mongo.db.speakers.find_one({'committee' : committee })
    find_user = mongo.db.mundb.find_one({'identifier' : user_id, 'committee' : committee})
    print(find_user)
    d_name = []
    d_country = []
    x = find_committee
    if x is None:
        d_name.append(find_user['name'])
        d_country.append(find_user['country'])
        mongo.db.speakers.insert_one({'committee' : committee, 'speakers' : d_name, 'country' : d_country})
        return dumps('success')
    else:
        if find_user['name'] in x['speakers']:
            return dumps('Already added')
        d_name = x['speakers']
        d_name.append(find_user['name'])
        d_country = x['country']
        d_country.append(find_user['country'])
        mongo.db.speakers.update_one({'committee' : committee}, { '$set' : {'speakers' : d_name, 'country' : d_country }})
        return dumps('success')

if __name__ == '__main__':
    app.secret_key = 'mysecret'
    app.run(host='0.0.0.0', port=8888)
