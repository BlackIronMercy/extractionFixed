# https://jike.in/qa/?qa=893722/

import cv2
import pytesseract
import numpy as np
from pdf2image import convert_from_path
from datetime import datetime
import sys

now = datetime.now()

current_time = now.strftime("%H:%M:%S")
print("Start Time =", current_time)

print("Path :", sys.argv[1])

pytesseract.pytesseract.tesseract_cmd = 'C:\\Program Files\\Tesseract-OCR\\tesseract.exe'

images = convert_from_path(sys.argv[1], 200,  poppler_path=r'C:\\Program Files\\poppler-23.11.0\\Library\\bin')

for x in images:
    x.save('output.jpg', 'JPEG')

    image = cv2.imread('output.jpg')
    result = image.copy()
    gray = cv2.cvtColor(image,cv2.COLOR_BGR2GRAY)
    thresh = cv2.threshold(gray, 0, 255, cv2.THRESH_BINARY_INV + cv2.THRESH_OTSU)[1]

    # Remove horizontal lines
    horizontal_kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (40,1))
    remove_horizontal = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, horizontal_kernel, iterations=2)
    cnts = cv2.findContours(remove_horizontal, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    cnts = cnts[0] if len(cnts) == 2 else cnts[1]
    print("hori lines =", len(cnts))
    for c in cnts:
        cv2.drawContours(result, [c], -1, (255, 255, 255), 10)

    # Remove vertical lines
    vertical_kernel = cv2.getStructuringElement(cv2.MORPH_RECT, (1,40))
    remove_vertical = cv2.morphologyEx(thresh, cv2.MORPH_OPEN, vertical_kernel, iterations=2)
    cnts = cv2.findContours(remove_vertical, cv2.RETR_EXTERNAL, cv2.CHAIN_APPROX_SIMPLE)
    cnts = cnts[0] if len(cnts) == 2 else cnts[1]
    print("Verti lines =", len(cnts))
    for c in cnts:
        cv2.drawContours(result, [c], -1, (255,255,255), 10)

    # cv2.imshow('thresh', thresh)
    # cv2.imshow('result', result)
    result = cv2.cvtColor(result,cv2.COLOR_BGR2GRAY)
    cv2.imwrite('result.png', result)
    cv2.waitKey()

    image = cv2.imread('result.png')
    
    custom_config = r'--oem 3 --psm 6'
    print(pytesseract.image_to_string(image, config=custom_config))

now = datetime.now()

current_time = now.strftime("%H:%M:%S")
print("Start Time =", current_time)
