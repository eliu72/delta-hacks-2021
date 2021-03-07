from flask import Flask, render_template, url_for, request, redirect
from flask_sqlalchemy import SQLAlchemy
from datetime import datetime
import cv2

import io
import os

# Imports the Google Cloud client library
from google.cloud import vision
from flask.wrappers import Response
import time

# run python flask in virtual env
# .\env\Scripts\activate.bat


# Instantiates a client
client = vision.ImageAnnotatorClient()

app = Flask(__name__)
app.config['SQLALCHEMY_DATABASE_URI'] = 'sqlite:///test.db'
db = SQLAlchemy(app)

global live 
live = True

global bp, bp_value
bp = 'null'
bp_value = 'null'

class HealthRecord(db.Model):
    id = db.Column(db.Integer, primary_key=True)
    diastolic = db.Column(db.Integer, nullable=False)
    systolic = db.Column(db.Integer, nullable=False)
    pulse = db.Column(db.Integer, nullable=False)

    # default time is utc timezone
    date_created = db.Column(db.DateTime, default=datetime.utcnow)

    def __repr__(self):
        return '<Record %r>' % self.id

@app.route('/', methods=['POST', 'GET'])
def index():
    global bp, bp_value
    if request.method == 'POST':
        return 
    else:
        print(bp)

        return render_template('index.html', bp_conditions=bp, bp_value=bp_value)

# @app.route('/see_results', methods=['POST', 'GET'])
# def see_results():
#     global bp
#     return render_template('high-risk.html', bp_conditions=bp)

@app.route('/video_feed')
def video_feed():
    return Response(livestream(), mimetype='multipart/x-mixed-replace; boundary=frame')

#background process happening without any refreshing
def livestream():
    # print("Allow access to webcam? Y/N")

    # access = input()

    if True:

        # starting the webcam
        cam = cv2.VideoCapture(0)

        # name the window
        #cv2.namedWindow("Webcam Capture")

        img_counter = 0
        global live
        frame = None
        while(live):
            ret, frame = cam.read()
            if not ret:
                print("Failed to grab frame.")
                break
            
            ret, buffer = cv2.imencode('.jpg', frame)
            new_frame = buffer.tobytes()
            yield (b'--frame\r\n'b'Content-Type: image/jpeg\r\n\r\n' + new_frame + b'\r\n')  # concat frame one by one and show result
            
            # show the wecam to user
            #cv2.imshow('img1', frame)

            # esc key exits
            k = cv2.waitKey(1)

            # # space key is hit - capture image and store
            # if k % 256 == 32:

        # stores the image with the curr image count
        print(img_counter)
        img_name = "opencv_frame.jpg"
        cv2.imwrite(img_name, frame)
        print("Image taken.")
        img_counter += 1

        print("Closing app.")
        #break

        cam.release()
        cv2.destroyAllWindows()


@app.route('/background_process_test')
def background_process_test():
    # do stuff on button click
    global live
    global bp, bp_value
    live = False
    time.sleep(3)
    final_bp = identifyBP()
    print(final_bp)
    setBPConditions(final_bp)
    bp_value = str(final_bp[0]) + " / " + str(final_bp[1])

    return redirect('/')

@app.route('/restart')
def restart():
    reset()
    return Response(livestream(), mimetype='multipart/x-mixed-replace; boundary=frame')

def reset():
    global live
    live = True
    bp = 'null'
    bp_value = 'null'

def setBPConditions(final_bp):
    global bp
    if final_bp[0] == 0:
        return
    elif (final_bp[0] <= 120) and (final_bp[1] <= 80):
        bp = "low"
        return
    elif (final_bp[0] <= 139) and (final_bp[1] <= 89):
        bp = "med"
        return
    else:
        bp = "high"
        return
    

def processTexts(texts):
    final_bp = [0,0,0]

    i = 0
    for text in texts:
        if text.description.isnumeric():
            final_bp[i]=int(text.description)
            i += 1
    
    return final_bp

def identifyBP():
    # The name of the image file to annotate
    file_name = os.path.abspath('opencv_frame.jpg')

    # Loads the image into memory
    with io.open(file_name, 'rb') as image_file:
        content = image_file.read()

    image = vision.Image(content=content)

    # Performs label detection on the image file
    response = client.text_detection(image=image)
    texts = response.text_annotations

    print('Texts:')
    for text in texts:
        print('\n"{}"'.format(text.description))

        vertices = (['({},{})'.format(vertex.x, vertex.y)
                    for vertex in text.bounding_poly.vertices])

        print('bounds: {}'.format(','.join(vertices)))
    
    final_bp = processTexts(texts)

    return final_bp
    
    if response.error.message:
        raise Exception(
            '{}\nFor more info on error messages, check: '
            'https://cloud.google.com/apis/design/errors'.format(
                response.error.message))

if __name__ == "__main__":
    app.run(debug=True)