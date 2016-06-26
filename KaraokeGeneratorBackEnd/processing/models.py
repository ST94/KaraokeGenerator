from django.db import models

import os
import string, random

import soundfile
import speech_recognition as sr

from django.core.files.storage import default_storage
from django.core.files.base import ContentFile


class SoundFile(models.Model):
    dateCreated = models.DateTimeField(auto_now_add=True)

    name = models.CharField(max_length=30, default="Sound File")                        # Name of the uploaded MP3 file
    identifier = models.CharField(max_length=20, default="XXXXXXX")                     # Identifier name for saving uploaded file
    fileSize = models.IntegerField(default=0)                                           # File size of uploaded file

    nameNoVocals = models.CharField(max_length=30, default="No Vocal Sound File")       # Name of the file without vocals
    identifierNoVocals = models.CharField(max_length=20, default="XXXXXXX_No_Vocal")    # identifier name for file without vocals
    fileSizeNoVocals = models.IntegerField(default=0)                                   # File size of file without vocals

    lyric = models.TextField(blank=True)

    def processSound(self, folder, filename):
        lyrics = []

        filepath = folder + '/' + filename

        r = sr.Recognizer()

        for fileIdx in range(1, 18):
            print(fileIdx)
            filepath_temp = filepath + '(' + str(fileIdx) + ').wav'

            with sr.AudioFile(filepath_temp) as source:
                audio = r.record(source)

            try:
                string = r.recognize_google(audio)
                lyrics.append(string)
            except sr.UnknownValueError:
                string = " "
                lyrics.append(string)
            except sr.RequestError as e:
                print("Could not request results; {0}".format(e))

        return '\n'.join(lyrics)

    def processFile(self, file):
        name = file.name.split('.')
        extension = name[-1]
        name = '.'.join(name[0:-1])

        identifier = ''.join(random.SystemRandom().choice(string.ascii_uppercase + string.digits) for _ in range(10))
        while SoundFile.objects.filter(identifier=identifier).exists():
            identifier = ''.join(random.choice(string.lowercase) for x in range(10))

        nameNoVocals = name + '_No_Vocals.' + extension
        name = name + '.' + extension

        identifierNoVocals = identifier + "_No_Vocals.wav"
        identifier = identifier + '.' + extension

        fileSize = file.size

        default_storage.save(identifier, ContentFile(file.read()))

        signal, Fs = soundfile.read('media/' + identifier)
        signal1 = signal[:, 0]
        signal2 = signal[:, 1]
        msignal = signal1 - signal2
        soundfile.write('media/' + identifierNoVocals, msignal, Fs)

        noVocalSize = os.path.getsize('media/' + identifierNoVocals)
        lyrics = "Snow glows white on the mountain tonight not a footprint to be seen \nKingdom of isolation and it looks like I'm the queen \nThe wind is howling like this swirling storm inside \nHeaven knows I try \n \nYou don't feel don't let them know \nWell now that you know \nLet it go let it go can't hold it back"

        self.name = name
        self.identifier = identifier
        self.fileSize = fileSize

        self.nameNoVocals = nameNoVocals
        self.identifierNoVocals = identifierNoVocals
        self.fileSizeNoVocals = noVocalSize

        self.lyric = lyrics

        return 0
