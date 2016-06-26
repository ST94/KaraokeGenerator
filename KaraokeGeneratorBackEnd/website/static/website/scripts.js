
$(function() {
    $('input[type=file]').change(function(){
        $(this).simpleUpload('http://3b53f00f.ngrok.io/api/song/', {
            init: function(total_uploads) {

            },

            finish: function() {

            },

            start: function(file){            
                this.fileName = file.name;
            },

            success: function(data){
                console.log(data);
                $('.output').show();
                $('.lyrics').show();
                $('.output-text').html(data['song'].link(data['song']));
                $('.lyrics-text').html(data['lyrics'].replace("\n", "<br>"));
            },

            error: function(error){

            }
        });
    });
});


