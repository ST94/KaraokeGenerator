from django.contrib import admin

from .models import SoundFile


class SoundFileAdmin(admin.ModelAdmin):
    list_display = ('id', 'name', 'identifier', 'fileSize', 'nameNoVocals', 'identifierNoVocals', 'fileSizeNoVocals', 'dateCreated')


admin.site.register(SoundFile, SoundFileAdmin)
