from rest_framework import serializers

from processing.models import SoundFile


class SoundFileSerializer(serializers.ModelSerializer):

    class Meta:
        model = SoundFile
        fields = ('id', 'dateCreated', 'name', 'identifier', 'fileSize', 'lyric', 'nameNoVocals', 'identifierNoVocals', 'fileSizeNoVocals')
