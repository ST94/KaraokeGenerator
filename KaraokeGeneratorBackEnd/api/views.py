from decimal import *
from django.views.decorators.csrf import csrf_exempt

from rest_framework import viewsets
from rest_framework.decorators import api_view
from rest_framework.response import Response

from processing.models import SoundFile
from .serializers import SoundFileSerializer


class SoundFileViewSet(viewsets.ModelViewSet):
    queryset = SoundFile.objects.all()
    serializer_class = SoundFileSerializer

    @csrf_exempt
    def create(self, request):
        status = {}
        status_code = 400

        try:
            request.FILES
        except Exception as e:
            status = str(e)
        else:
            file = request.FILES.get('file', False)

            if file is not False:
                soundFile = SoundFile()
                soundFile.processFile(file)
                soundFile.save()

                print(soundFile.__dict__)

                status_code = 200
                status = {
                    'song': 'http://' + request.get_host() + '/media/' + soundFile.identifierNoVocals,
                    'name': soundFile.nameNoVocals,
                    'identifier': soundFile.identifierNoVocals,
                    'lyrics': soundFile.lyric,
                    'status_code': status_code
                }
            else:
                status = {
                    'error': "File doesn't exist"
                }

        return Response(status, status=status_code)


@api_view(('GET',))
def api_root(request, format=None):
    return Response({
        'soundFile': reverse('soundFile-list', request=request, format=format)
    })
