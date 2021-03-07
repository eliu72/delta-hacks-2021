import cv2 


print("Allow access to webcam? Y/N")

access = input()

if access == 'Y' or access == 'y':

    # starting the webcam
    cam = cv2.VideoCapture(0)

    # name the window
    cv2.namedWindow("Webcam Capture")

    img_counter = 0

    while(True):
        ret, frame = cam.read()
        if not ret:
            print("Faile to grab frame.")
            break
        
        # show the wecam to user
        cv2.imshow('img1', frame)

        # esc key exits
        k = cv2.waitKey(1)
        if k % 256 == 27:
            print("Closing app.")
            break

        # space key is hit - capture image and store
        elif k % 256 == 32:

            # stores the image with the curr image count
            img_name = "opencv_frame_{}.png".format(img_counter)
            cv2.imwrite(img_name, frame)
            print("Image taken.")
            img_counter += 1

    cam.release()
    cv2.destroyAllWindows()